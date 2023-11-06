package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class NoAwsSongException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public NoAwsSongException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
