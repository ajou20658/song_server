package com.example.cleancode.utils.CustomException;

import com.example.cleancode.user.entity.UserSong;
import lombok.Getter;

@Getter
public class DjangoRequestException extends RuntimeException{
    private final ExceptionCode exceptionCode;
    public DjangoRequestException(ExceptionCode exceptionCode){
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}