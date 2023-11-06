package com.example.cleancode.utils.CustomException;

import lombok.Getter;

@Getter
public class JwtIssueException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public JwtIssueException(ExceptionCode exceptionCode){
        this.exceptionCode = exceptionCode;
    }
}