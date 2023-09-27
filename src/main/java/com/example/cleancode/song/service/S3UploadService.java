package com.example.cleancode.song.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    private final SongRepository songRepository;
    private final WebClient webClient;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public Resource stream(String url){
        S3Object s3Object = amazonS3.getObject(bucket,url);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());
        return resource;
    }
    //input : 원곡
    //logic : vocal + instru
    public void split(MultipartFile multipartFile) throws IOException {
        String path = "/home/ubuntu/2023-2/paran/song_server/src/main/resources/static/";
        String name = multipartFile.getOriginalFilename();
        multipartFile.transferTo(new File(path+name));
        String input_path = path+name;
        String url = "http://localhost:8000/separate_audio?input_file="+input_path+"&output_dir="+path;
        webClient.get()
                .uri(url);
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
