package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class JwtExpireException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public JwtExpireException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
