package com.example.cleancode.song.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.entity.UserPrinciple;
import com.example.cleancode.user.entity.UserSong;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;

    @Autowired
    private UserSongRepository userSongRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public boolean userFile(String folder, MultipartFile multipartFile,String id) throws IOException{
        String originalFilename = multipartFile.getOriginalFilename();

        String filename = folder+"/"+id+"_"+originalFilename;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
        amazonS3.getUrl(bucket,originalFilename).toString();
        userSongRepository.save(UserSong.builder()
                .url(filename)
                .userid(Long.valueOf(id))
                .build());
        return true;
    }
//    public void deleteFile(String fileName) throws IOException{
//        try{
//            amazonS3.deleteObject(bucket,);
//        }
//    }
    public Resource stream(String url){
        S3Object s3Object = amazonS3.getObject(bucket,url);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());
        return resource;
    }
}
