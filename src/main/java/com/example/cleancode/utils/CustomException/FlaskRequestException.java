package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class FlaskRequestException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public FlaskRequestException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
