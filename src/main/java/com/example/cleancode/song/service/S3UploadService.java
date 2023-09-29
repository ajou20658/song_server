package com.example.cleancode.song.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    private final SongRepository songRepository;
    private final WebClient webClient;
    private final RestTemplate restTemplate;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public Resource stream(String url){
        S3Object s3Object = amazonS3.getObject(bucket,url);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());
        return resource;
    }
    //input : 원곡
    //logic : vocal + instru
    public void split(MultipartFile multipartFile, Long songId, @Nullable Long userId){
        String path = "/home/ubuntu/git/song_server/src/main/resources/static/";
        String name = multipartFile.getOriginalFilename();
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = multipartFile.getBytes();
        }catch (IOException ex){
            log.info(ex.getMessage());
        }
        Resource resource = new ByteArrayResource(fileBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file",resource);
        body.add("songId",songId);
        body.add("userId",userId);
        body.add("output_dir",path);
        body.add("isUser",true);
        String url = "http://localhost:8000/seperate_audio/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        log.info("status code = {}",response.getStatusCode());
    }
    private boolean vocalUpload(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String[] concat = originalFilename.split("_");
        Optional<Song> chart100Optional = songRepository.findByArtistAndTitle(concat[2],concat[1]);
        if(chart100Optional.isEmpty()){
            return false;
        }
        SongDto songDto = chart100Optional.get().toChartDto();
        String filename = "vocal/"+ songDto.getId();
        songDto.setVocalUrl(filename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            songRepository.save(songDto.toChartEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
    private boolean instUpload(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String[] concat = originalFilename.split("_");
        Optional<Song> chart100Optional = songRepository.findByArtistAndTitle(concat[2],concat[1]);
        if(chart100Optional.isEmpty()){
            return false;
        }
        SongDto songDto = chart100Optional.get().toChartDto();
        String filename = "inst/"+ songDto.getId();
        songDto.setInstUrl(filename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            songRepository.save(songDto.toChartEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
}
