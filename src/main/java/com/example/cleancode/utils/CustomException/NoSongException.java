package com.example.cleancode.utils.CustomException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class NoSongException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    private final String detailMessage;
    public NoSongException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.detailMessage = exceptionCode.getMessage();
    }
}
