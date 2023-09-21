package com.example.cleancode.song.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.cleancode.song.dto.ChartDTO;
import com.example.cleancode.song.entity.Chart100;
import com.example.cleancode.song.repository.ChartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    private final ChartRepository chartRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public Resource stream(String url){
        S3Object s3Object = amazonS3.getObject(bucket,url);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());
        return resource;
    }
    //input : 원곡
    //logic : vocal + instru
//    @Transactional
//    public boolean FileUpload(Long SongId, MultipartFile multipartFile){
//        String originalFilename = multipartFile.getOriginalFilename();
//
//    }
    public boolean vocalUpload(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String[] concat = originalFilename.split("_");
        Optional<Chart100> chart100Optional = chartRepository.findByArtistAndTitle(concat[2],concat[1]);
        if(chart100Optional.isEmpty()){
            return false;
        }
        ChartDTO chartDTO = chart100Optional.get().toChartDto();
        String filename = "vocal/"+chartDTO.getId();
        chartDTO.setVocalUrl(filename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            chartRepository.save(chartDTO.toChartEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
    public boolean instUpload(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String[] concat = originalFilename.split("_");
        Optional<Chart100> chart100Optional = chartRepository.findByArtistAndTitle(concat[2],concat[1]);
        if(chart100Optional.isEmpty()){
            return false;
        }
        ChartDTO chartDTO = chart100Optional.get().toChartDto();
        String filename = "inst/"+chartDTO.getId();
        chartDTO.setInstUrl(filename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            chartRepository.save(chartDTO.toChartEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
}
