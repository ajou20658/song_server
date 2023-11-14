package com.example.cleancode.aws.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.NoAwsSongException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public Resource stream(String url) {
        try {
            S3Object s3Object = amazonS3.getObject(bucket, url);
            try (InputStream inputStream = s3Object.getObjectContent()) {
                // 필요한 작업 수행 (예: 데이터를 읽거나 복사)

                // 스트림을 소비한 후에 InputStreamResource로 감싸서 반환
                return new InputStreamResource(inputStream);
            }
        } catch (SdkClientException | IOException e) {
            throw new NoAwsSongException(ExceptionCode.AWS_ERROR);
        }
    }

    public Resource miniStream(String url){
        try{
            S3Object s3Object = amazonS3.getObject(bucket,url);
            InputStream inputStream = s3Object.getObjectContent();
            byte[] audioData = readAudioDate(inputStream,60);
            return new InputStreamResource(new ByteArrayInputStream(audioData));
        }catch (SdkClientException e){
            throw new NoAwsSongException(ExceptionCode.AWS_ERROR);
        }
    }

    private byte[] readAudioDate(InputStream inputStream,int durationInSecond){
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytesRead = 0;
        int maxBytesToRead = durationInSecond * 1024;
        ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
        try{
            while (totalBytesRead < maxBytesToRead && (bytesRead = inputStream.read(buffer)) != -1) {
                audioBuffer.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }catch (IOException e){
            log.info(e.getMessage());
        }
        return audioBuffer.toByteArray();
    }

    private Resource createWavFile(byte[] audioData){
        AudioFormat format = new AudioFormat(44100,16,1,true,false);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream,format,audioData.length/2);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try{
            AudioSystem.write(audioInputStream,AudioFileFormat.Type.WAVE,byteArrayOutputStream);
        }catch (IOException e){
            log.info(e.getMessage());
        }
        return new InputStreamResource(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    }
    //input : 원곡
    //logic : vocal + instr
}
