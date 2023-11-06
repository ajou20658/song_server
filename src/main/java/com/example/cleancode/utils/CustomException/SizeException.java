package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class SizeException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public SizeException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
