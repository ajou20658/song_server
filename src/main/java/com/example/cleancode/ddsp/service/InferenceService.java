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
import com.example.cleancode.user.entity.GenreCountFrame;
import com.example.cleancode.user.entity.RecommandRequestDataFrame;
import com.example.cleancode.user.entity.Spectr2DataFrame;
import com.example.cleancode.utils.CustomException.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InferenceService {
    private final ResultSongRepository resultSongRepository;
    private final SongRepository songRepository;
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
                .onStatus(HttpStatusCode::isError
                        ,ClientResponse::createError)
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMinutes(5))
                .subscribe(res-> {
                    log.info("노래 생성 완료 {} : {}",song.getArtist(),song.getTitle());
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
            try{
                amazonS3.deleteObject(bucket,resultSong.getGeneratedUrl().replace("generate","generated"));
            }catch (Exception e2){
                log.error("aws에 존재하지 않음");
            }
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
    @Transactional
    public void updateRecommandList(Long ptrId){
        PtrData ptrData = validator.ptrDataValidator(ptrId);
        WebClient webClient = WebClient
                .builder()
                .build();
        GenreCountFrame user_genre = GenreCountFrame.builder()
                .a(0).b(2).c(2).d(2).e(2).f(2)
                .g(2).h(2).i(2).j(0).k(2).l(0).m(2)
                .build();
        List<Integer> spectrum = ptrData.getSpectr();
        Collections.sort(spectrum);
        Spectr2DataFrame user_f0 = new Spectr2DataFrame(
                spectrum.get(0),
                spectrum.get(1),
                spectrum.get(2),
                spectrum.get(3),
                spectrum.get(4),
                spectrum.get(5),
                spectrum.get(6),
                spectrum.get(7)
        );
        RecommandRequestDataFrame user_info_json =
                new RecommandRequestDataFrame(user_f0,user_genre);
        ObjectMapper objectMapper =new ObjectMapper();
        String json="";
        try {
            json = objectMapper.writeValueAsString(Collections.singletonMap("user_info_json",user_info_json));
            log.info(json);
        }catch (Exception e){
            log.error("json 변환 오류");
        }
        List<Long> recommandList = webClient.post()
                .uri("http://localhost:8000/SongRecommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(json))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody->{
                    try{
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        JsonNode songIdNode = jsonNode.get("song_id");
                        List<Long> ids = new ArrayList<>();
                        if(songIdNode!=null&&songIdNode.isArray()){
                            for(JsonNode iNode:songIdNode){
                                ids.add(iNode.asLong());
                            }
                        }
                        return Mono.just(ids);
                    }catch (Exception e){
                        log.error(e.getMessage());
                        return Mono.error(e);
                    }
                })
                .block();
        ptrData.setRecommandSongIds(recommandList);

    }
    public List<Song> getRecommandList(Long ptrid){
        PtrData ptrData = validator.ptrDataValidator(ptrid);
        List<Long> songNumList = ptrData.getRecommandSongIds();
        List<Song> songList = new ArrayList<>();
        for(Long i: songNumList){
            songList.add(validator.songValidator(i));
        }
        return songList;
    }
}
