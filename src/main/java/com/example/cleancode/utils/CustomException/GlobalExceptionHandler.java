package com.example.cleancode.utils.CustomException;

import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.user.entity.UserSong;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
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
    public ResponseEntity<Object> expiredJwt(ExpiredJwtException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",HttpStatus.FORBIDDEN);
        response.put("message","재갱신이 필요합니다");
        return new ResponseEntity<>(response,HttpStatus.IM_USED);
    }
    @ExceptionHandler({JwtIssueException.class,
        JwtExpireException.class,
        NoUserSongException.class,
        NoUserException.class,
        NoSongException.class,
        FormatException.class,
        DjangoRequestException.class,
        NoAwsSongException.class,
        SizeException.class,
        FormatException.class,
        NoPtrException.class,
        AwsUploadException.class
    })
    public ResponseEntity<Object> customExceptionHandler(JwtIssueException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",e.getExceptionCode().getStatus());
        response.put("message",e.getExceptionCode().getMessage());
        return new ResponseEntity<>(response,e.getExceptionCode().getStatus());
    }
}
