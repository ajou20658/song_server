package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class NoPtrException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public NoPtrException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
