package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class NoGeneratedSongException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public NoGeneratedSongException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
