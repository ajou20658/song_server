package com.example.cleancode.ddsp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.ddsp.entity.InferenceQueue;
import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import com.example.cleancode.ddsp.entity.etc.InferenceRedisEntity;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.ddsp.repository.ResultSongRepository;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.utils.CustomException.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InferenceService {
    private final ResultSongRepository resultSongRepository;
    private final InferenceQueue inferenceQueue;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    @Transactional
    public void inferenceStart(Long ptrId, Long songId) {
        PtrData ptrData = validator.ptrDataValidator(ptrId);
        Song song = validator.songValidator(songId);
        String ptrKey = ptrData.getPtrUrl();
        String songKey = song.getOriginUrl();
        String uuid = String.valueOf(UUID.randomUUID());
        InferenceRedisEntity inferenceRedisEntity = InferenceRedisEntity.builder()
                .ptrId(String.valueOf(ptrId))
                .songId(String.valueOf(songId))
                .uuid(uuid)
                .build();

        try {
            String url = "/songssam/voiceChangeModel/";
            MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
            body.add("wav_path",songKey);
            body.add("fPtrPath",ptrKey);
            body.add("uuid",uuid);
            flaskRequest(url, body, ptrData, song,inferenceRedisEntity);
        }catch (Throwable e){
            inferenceQueue.changeStatus(inferenceRedisEntity,ProgressStatus.ERROR);
            throw new DjangoRequestException(ExceptionCode.WEB_CLIENT_ERROR);
        }
    }
    @Async
    @Transactional
    public void flaskRequest(String url,
                             MultiValueMap<String,String> body,
                             PtrData ptrData,
                             Song song,
                             InferenceRedisEntity inferenceRedisEntity) {
//        inferenceQueue.pushInProgress(inferenceRedisEntity);
        WebClient webClient = WebClient
                .builder()
                .baseUrl("http://"+djangoUrl)
                .build();
        webClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMinutes(5))
                .handle((JsonNode,sink)->{
                    String uuid = JsonNode.get("uuid").asText();
                    if(uuid==null) {
                        sink.error(new DjangoRequestException(ExceptionCode.WEB_CLIENT_ERROR));
                        return;
                    }
                    sink.next(uuid);
                })
                .subscribe(res-> {
                    String filename = "generated/" + inferenceRedisEntity.getUuid();
                    ResultSong resultSong = ResultSong.builder()
                            .generatedUrl(filename)
                            .song(song)
                            .ptrData(ptrData)
                            .build();
                    resultSongRepository.save(resultSong);
                }
        );
        inferenceQueue.changeStatus(inferenceRedisEntity, ProgressStatus.COMPLETE);
    }
    public List<ResultSong> allResult(Long ptrId){
        PtrData ptrData = validator.ptrDataValidator(ptrId);
        return resultSongRepository.findResultSongsByPtrData(ptrData);
    }
    public void songDelete(Integer generatedSongId) throws NoAwsSongException{
        ResultSong resultSong=validator.resultSongValidator(generatedSongId);
        try{
            amazonS3.deleteObject(bucket,resultSong.getGeneratedUrl());
        }catch (Exception e){
            throw new NoAwsSongException(ExceptionCode.AWS_ERROR);
        }
        try{
            amazonS3.deleteObject(bucket,resultSong.getGeneratedUrl().replace("generate","generated"));
        }catch (Exception e){
            throw new NoAwsSongException(ExceptionCode.AWS_ERROR);
        }
        resultSongRepository.delete(resultSong);
    }
    public String showStatus(Long ptrId,Long songId){
        InferenceRedisEntity inferenceRedisEntity = InferenceRedisEntity.builder()
                .ptrId(String.valueOf(ptrId))
                .songId(String.valueOf(songId))
                .build();
        return inferenceQueue.getData(inferenceRedisEntity).getMessage();
    }


}
