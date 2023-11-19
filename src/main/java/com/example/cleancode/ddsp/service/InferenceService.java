package com.example.cleancode.ddsp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.ddsp.repository.ResultSongRepository;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.utils.CustomException.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InferenceService {
    private final SongRepository songRepository;
    private final PtrDataRepository ptrDataRepository;
    private final ResultSongRepository resultSongRepository;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    private final WebClient webClient;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    @Transactional
    public Mono<Integer> inferenceStart(Long ptrId, Long songId) {
        PtrData ptrData = validator.ptrDataValidator(ptrId);
        Song song = validator.songValidator(songId);
        String ptrKey = ptrData.getPtrUrl();
        String songKey = song.getOriginUrl();

        return flaskRequest(ptrKey, songKey)
                .flatMap(response -> {
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(response.length);
                    metadata.setContentType("audio/mpeg");

                    String filename = "generate/" + UUID.randomUUID();
                    InputStream inputStream = new ByteArrayInputStream(response);

                    try {
                        amazonS3.putObject(bucket, filename, inputStream, metadata);
                    } catch (Exception e) {
                        log.error("응답 에러", e.getMessage());
                        return Mono.error(new AwsUploadException(ExceptionCode.AWS_ERROR));
                    }

                    ResultSong resultSong = ResultSong.builder()
                            .generatedUrl(filename)
                            .song(song)
                            .ptrData(ptrData)
                            .build();

                    ResultSong result = resultSongRepository.save(resultSong);
                    return Mono.just(result.getId());
                })
                .onErrorResume(throwable -> {
                    // 에러 처리 로직
                    return Mono.error(throwable);
                });
    }
    @Transactional
    public Mono<byte[]> flaskRequest(String ptrKey, String songKey){
        //ptrKey는 ptr/uuid
        //songKey는 Origin/uuid
        String uuid = songKey.split("/")[1];
        try {
            String encodedSongKey = URLEncoder.encode(songKey, "UTF-8");
            String encodedPtrKey = URLEncoder.encode(ptrKey,"UTF-8");
            String url = "http://" + djangoUrl + "/songssam/voiceChangeModel/?wav_path=" + encodedSongKey +
                    "&fPtrPath=" + encodedPtrKey +
                    "&uuid=" + uuid;
            return webClient.get()
                    .uri(url)
                    .accept(MediaType.valueOf("audio/mpeg"))
                    .retrieve()
                    .bodyToMono(byte[].class);
        }catch (Exception e){
            e.printStackTrace();
            return Mono.error(e);
        }
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

}
