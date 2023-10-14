package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class FormatException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public FormatException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
