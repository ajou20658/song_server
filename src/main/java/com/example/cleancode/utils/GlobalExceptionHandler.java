package com.example.cleancode.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnsupportedJwtException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> badRequest(UnsupportedJwtException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",HttpStatus.UNAUTHORIZED);
        response.put("message","토큰 오류, 새로운 로그인 필요");
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ExpiredJwtException.class)
//    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> expiredJwt(ExpiredJwtException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",HttpStatus.FORBIDDEN);
        response.put("message","재갱신이 필요합니다");
        return new ResponseEntity<>(response,HttpStatus.IM_USED);
    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> ioBadRequest(IOException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",HttpStatus.UNAUTHORIZED);
        response.put("message","IOException 발생");
        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }
//    @ExceptionHandler(SizeLimitExceededException)
}
