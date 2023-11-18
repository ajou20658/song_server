package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class AwsUploadException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public AwsUploadException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
