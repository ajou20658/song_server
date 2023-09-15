package com.example.cleancode.song.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String saveFile(String folder, MultipartFile multipartFile) throws IOException{
        String originalFilename = multipartFile.getOriginalFilename();
//        String extension = multipartFile.getName().substring()
        String filename = folder+"/"+originalFilename;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3.putObject(bucket,originalFilename,multipartFile.getInputStream(),metadata);
        return amazonS3.getUrl(bucket,originalFilename).toString();
    }
//    public void deleteFile(String fileName) throws IOException{
//        try{
//            amazonS3.deleteObject(bucket,);
//        }
//    }
}
