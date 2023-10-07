package com.example.cleancode.config;

import com.amazonaws.services.s3.transfer.Upload;
import com.example.cleancode.aws.entity.UploadStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class GlobalArray {
    @Bean
    public Map<String, UploadStatus> userUploadStatusMap() {
        return new ConcurrentHashMap<>();
    }
    @Bean
    public Map<String, UploadStatus> songUploadStatusMap(){
        return new ConcurrentHashMap<>();
    }
}
