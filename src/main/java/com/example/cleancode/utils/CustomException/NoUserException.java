package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class NoUserException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    private final String detailMessage;
    public NoUserException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.detailMessage = exceptionCode.getMessage();
    }
}
