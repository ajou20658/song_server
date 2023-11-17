package com.example.cleancode.ddsp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.utils.CustomException.AwsUploadException;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class TrainService {
    private final PtrDataRepository ptrDataRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public void ptrFileUplaod(MultipartFile file,String name){
        String filename = "ptr/"+ UUID.randomUUID();
        try{
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getBytes().length);
            metadata.setContentType(file.getContentType());
            amazonS3.putObject(bucket,filename,file.getInputStream(),metadata);
        }catch (Exception e){
            throw new AwsUploadException(ExceptionCode.AWS_UPLOAD_ERROR);
        }
        PtrData ptrData = PtrData.builder()
                .ptrUrl(filename)
                .name(name)
                .build();
        ptrDataRepository.save(ptrData);
    }
}
