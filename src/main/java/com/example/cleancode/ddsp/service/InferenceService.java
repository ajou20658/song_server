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
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.flask-url}")
    private String flaskUrl;
    @Transactional
    public Integer inferenceStart(Long ptrId,Long songId){
        PtrData ptrData = validator.ptrDataValidator(ptrId);
        Song song = validator.songValidator(songId);
        String ptrKey = ptrData.getPtrUrl();
        String songKey = song.getOriginUrl();
        byte[] response = flaskRequest(ptrKey,songKey);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(response.length);
        metadata.setContentType("audio/mpeg");

        String filename = "generate/"+ UUID.randomUUID();
        InputStream inputStream = new ByteArrayInputStream(response);
        try {
            amazonS3.putObject(bucket, filename, inputStream, metadata);
        }catch (Exception e){
            throw new AwsUploadException(ExceptionCode.AWS_ERROR);
        }
        ResultSong resultSong = ResultSong.builder()
                .generatedUrl(filename)
                .song(song)
                .ptrData(ptrData)
                .build();
        ResultSong result = resultSongRepository.save(resultSong);
        return result.getId();
    }
    @Transactional
    public byte[] flaskRequest(String ptrKey, String songKey){
        //ptrKey는 ptr/uuid
        //songKey는 Origin/uuid
        String uuid = songKey.split("/")[1];
        WebClient webClient = WebClient.builder()
                .baseUrl("http://"+flaskUrl)
                .build();
        String url = "/voiceChangeModel?wav_path={param1}&fPtrPath={param2}&uuid={param3}";
        String param1=songKey;
        String param2=ptrKey;
        String param3=uuid;
        Mono<byte[]> response = webClient.get()
                .uri(url,param1,param2,param3)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchangeToMono(clientResponse -> {
                    if(clientResponse.statusCode().is2xxSuccessful()){
                        return clientResponse.bodyToMono(byte[].class);
                    }else{
                        log.error("Flask Req/Res Error"+clientResponse.statusCode());
                        throw new FlaskRequestException(ExceptionCode.WEB_CLIENT_ERROR);
                    }
                });
        return response.block();
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
