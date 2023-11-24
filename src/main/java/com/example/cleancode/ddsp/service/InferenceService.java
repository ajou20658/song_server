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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
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
    private final WebClient webClient;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    private final static ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs()
                            .maxInMemorySize(1024*1024*20))
            .build();
    @Transactional
    public void inferenceStart(Long ptrId, Long songId) {
        PtrData ptrData = validator.ptrDataValidator(ptrId);
        Song song = validator.songValidator(songId);
        String ptrKey = ptrData.getPtrUrl();
        String songKey = song.getOriginUrl();
        InferenceRedisEntity inferenceRedisEntity = InferenceRedisEntity.builder()
                .ptrId(String.valueOf(ptrId))
                .songId(String.valueOf(songId))
                .build();
        WebClient webClient = WebClient.builder()
                .baseUrl("http://" + djangoUrl)
                .exchangeStrategies(exchangeStrategies)
                .build();
        String uuid = songKey.split("/")[1];

        try {
            String url = "/songssam/voiceChangeModel/?wav_path=" + songKey +
                    "&fPtrPath=" + ptrKey +
                    "&uuid=" + uuid;
            inferenceQueue.pushInProgress(inferenceRedisEntity);
            Mono<byte[]> response = webClient.get()
                    .uri(url)
                    .accept(MediaType.ALL)
                    .retrieve()
                    .onStatus(
                            httpStatusCode -> httpStatusCode.is5xxServerError() ||
                            httpStatusCode.is4xxClientError(),
                            clientResponse -> Mono.error(new DjangoRequestException(ExceptionCode.WEB_CLIENT_ERROR))
                    )
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofMinutes(5));

            flaskRequest(response ,ptrData, song,inferenceRedisEntity);
        }catch (Exception e){
            inferenceQueue.changeStatus(inferenceRedisEntity,ProgressStatus.ERROR);
            throw new DjangoRequestException(ExceptionCode.WEB_CLIENT_ERROR);
        }
    }
    @Async
    @Transactional
    public void flaskRequest(Mono<byte[]> response,PtrData ptrData,Song song,InferenceRedisEntity inferenceRedisEntity){
        response.subscribe(res->
                {
                    if(res==null){
                        throw new NoGeneratedSongException(ExceptionCode.RESULT_SONG_ERROR);
                    }
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(res.length);
                    metadata.setContentType("audio/mpeg");

                    String filename = "generate/" + UUID.randomUUID();
                    InputStream inputStream = new ByteArrayInputStream(res);

                    try {
                        amazonS3.putObject(bucket, filename, inputStream, metadata);
                    } catch (Exception e) {
                        log.error("응답 에러", e.getMessage());
                        throw new AwsUploadException(ExceptionCode.AWS_ERROR);
                    }

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
    public void songDelete(Integer generatedSongId){
        ResultSong resultSong=validator.resultSongValidator(generatedSongId);
        try{
            amazonS3.deleteObject(bucket,resultSong.getGeneratedUrl());
        }catch (Exception e){
            throw new NoAwsSongException(ExceptionCode.AWS_ERROR);
        }
    }
    public String showStatus(Long ptrId,Long songId){
        InferenceRedisEntity inferenceRedisEntity = InferenceRedisEntity.builder()
                .ptrId(String.valueOf(ptrId))
                .songId(String.valueOf(songId))
                .build();
        return inferenceQueue.getData(inferenceRedisEntity).getMessage();
    }


}
