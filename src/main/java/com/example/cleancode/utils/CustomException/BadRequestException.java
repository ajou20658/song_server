package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public BadRequestException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
