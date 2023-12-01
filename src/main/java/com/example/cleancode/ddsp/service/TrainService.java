package com.example.cleancode.ddsp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.PtrDataUserDto;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.utils.CustomException.AwsUploadException;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.NoPtrException;
import com.example.cleancode.utils.CustomException.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
@Service
@Slf4j
@RequiredArgsConstructor
public class TrainService {
    private final PtrDataRepository ptrDataRepository;
    private final Validator validator;
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
    @Transactional
    public boolean ptrFileDelete(Long ptrId){
        PtrData ptrData1 = validator.ptrDataValidator(ptrId);
        try{
            amazonS3.deleteObject(bucket,ptrData1.getPtrUrl());
            ptrDataRepository.delete(ptrData1);
        }catch (Exception e){
            log.error("aws에 데이터 없음");
            return false;
        }
        return true;
    }
    public PtrData ptrFileUpdate(PtrDataUserDto ptrData){
        PtrData ptrData1 = validator.ptrDataValidator(ptrData.getId());
        return ptrDataRepository.save(ptrData1);

    }
}
