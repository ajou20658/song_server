package com.example.cleancode.song.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.FormatException;
import com.example.cleancode.utils.CustomException.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.HttpHead;
import org.openqa.jetty.http.HttpHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VocalPreProcessService {
    private final SongRepository songRepository;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    public boolean songUpload(MultipartFile multipartFile, Long songId){
        String type = multipartFile.getContentType();

        if((!Objects.requireNonNull(type).contains("audio"))){
            throw new FormatException(ExceptionCode.FORMAT_ERROR);
        }
        UUID uuid = UUID.randomUUID();
        String filename = "song/"+uuid;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());

        SongDto songDto = validator.songValidator(songId).toSongDto();
        songDto.setOriginUrl(filename);
        songDto.setStatus(ProgressStatus.UPLOADED);
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException("aws 업로드 에러");
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
    @Async
    @Transactional
    public void preprocess(Long songId){
        Song song = validator.songValidator(songId);
        String uuid = song.getOriginUrl().split("/")[1];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("fileKey",song.getOriginUrl());
        body.add("isUser","true");
        body.add("uuid",uuid);
        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body,headers);
        String url = djangoUrl + "/songssam/splitter/";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        // userSong Status변경
        SongDto songDto = song.toSongDto();
        songDto.setVocalUrl("vocal/"+uuid);
        songDto.setInstUrl("inst/"+uuid);
        songDto.setStatus(ProgressStatus.COMPLETE);
        song = songDto.toSongEntity();
        songRepository.save(song);
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
