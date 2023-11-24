package com.example.cleancode.song.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.entity.Dataframe2Json;
import com.example.cleancode.utils.CustomException.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VocalPreProcessService {
    private final SongRepository songRepository;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    @Transactional
    public boolean songUpload(MultipartFile multipartFile, Long songId){
        String type = multipartFile.getContentType();

        if((!Objects.requireNonNull(type).contains("mpeg"))){
            throw new FormatException(ExceptionCode.FORMAT_ERROR);
        }
        Song song = validator.songValidator(songId);
        String filename="";
        if(song.getOriginUrl()!=null){
            filename = "origin/"+song.getOriginUrl().split("/")[1];
        }else{
            filename = "origin/"+UUID.randomUUID();
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());


        SongDto songDto = validator.songValidator(songId).toSongDto();
        songDto.setOriginUrl(filename);
        songDto.setStatus(ProgressStatus.UPLOADED);
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
        }catch (IOException | SdkClientException ex){
            throw new AwsUploadException(ExceptionCode.AWS_ERROR);
        }
        songRepository.save(songDto.toSongEntity());
        return true;
    }
    public void convertWavToMp3(MultipartFile multipartFile, String outputFilePath){
        try{
            File tempWavFile = File.createTempFile("temp", ".wav");
            FileOutputStream fos = new FileOutputStream(tempWavFile);
            fos.write(multipartFile.getBytes());
            fos.close();
            File mp3File = File.createTempFile("temp", ".mp3");
            String[] lameCommand = {
                    "lame",
                    tempWavFile.getAbsolutePath(),
                    mp3File.getAbsolutePath()
            };
            ProcessBuilder processBuilder = new ProcessBuilder(lameCommand);

            // 작업 디렉토리 설정 (필요에 따라 변경 가능)
            processBuilder.directory(new File("/path/to/lame/directory"));

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("MP3 conversion completed successfully.");
                byte[] mp3Bytes = getBytesFromFile(mp3File);
            } else {
                System.out.println("MP3 conversion failed with exit code: " + exitCode);
            }

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
    //이곳은 노래 전처리 요청
    @Transactional
    public boolean preprocessStart(Long songId){
        Song song = validator.songValidator(songId);
        songRepository.save(song.changeStatus(ProgressStatus.PROGRESS));
        try{
            djangoRequest(song);
            log.info("django 요청");
        }catch (Exception ex){
            song.changeStatus(ProgressStatus.ERROR);
            songRepository.save(song);
            return false;
        }
        return true;
    }
    @Async
    @Transactional
    public void djangoRequest(Song song){
        String uuid = song.getOriginUrl().split("/")[1];
        WebClient webClient = WebClient
                .builder()
                .baseUrl("http://"+djangoUrl)
                .build();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("fileKey",song.getOriginUrl());
        body.add("isUser","false");
        body.add("uuid",uuid);

        String url = "/songssam/splitter/";
        try{
            webClient.post()
                .uri(url)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(JsonNode -> {
                    try{
                        String message = JsonNode.get("message").asText();
                        ObjectMapper objectMapper = new ObjectMapper();
                        Dataframe2Json[] result = objectMapper.readValue(message,Dataframe2Json[].class);
                        return result[0];
                    }catch (JsonProcessingException e){
                        log.error("파싱 에러");
                        throw new RuntimeException(e);
                    }
                })
                    .timeout(Duration.ofMinutes(5))
                .subscribe(response -> {
                    //여기 수정이 필요함
//                    log.info("status message = {}", response);
                    SongDto songDto = song.toSongDto();
                    songDto.setVocalUrl("vocal/"+uuid);
                    songDto.setInstUrl("inst/"+uuid);
                    songDto.setStatus(ProgressStatus.COMPLETE);
                    songDto.setSpectr(json2List(response));
                    songDto.toSongEntity();
                    songRepository.save(songDto.toSongEntity());
                });
        }catch (Exception e){
            throw new DjangoRequestException(ExceptionCode.WEB_SIZE_OVER);
        }
        // userSong Status변경
    }
    private List<Integer> json2List(Dataframe2Json rawJson){
        List<Integer> result = new ArrayList<>();
        result.add(rawJson.getF0_1());
        result.add(rawJson.getF0_2());
        result.add(rawJson.getF0_3());
        result.add(rawJson.getF0_4());
        result.add(rawJson.getF0_5());
        result.add(rawJson.getF0_6());
        result.add(rawJson.getF0_7());
        result.add(rawJson.getF0_8());
        log.info(result.toString());
        return result;
    }
    private static byte[] getBytesFromFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }

        fis.close();
        bos.close();

        return bos.toByteArray();
    }
}
