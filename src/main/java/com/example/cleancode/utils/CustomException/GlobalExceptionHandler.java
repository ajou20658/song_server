package com.example.cleancode.utils.CustomException;

import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.user.entity.UserSong;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    @ExceptionHandler(NoUserSongException.class)
    public ResponseEntity<Object> UserSongBadRequest(NoUserSongException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",e.getExceptionCode().getStatus());
        response.put("message",e.getExceptionCode().getMessage());
        return new ResponseEntity<>(response,e.getExceptionCode().getStatus());
    }
    @ExceptionHandler(NoUserException.class)
    public ResponseEntity<Object> UserBadRequest(NoUserException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",e.getExceptionCode().getStatus());
        response.put("message",e.getExceptionCode().getMessage());
        return new ResponseEntity<>(response,e.getExceptionCode().getStatus());
    }
    @ExceptionHandler(NoSongException.class)
    public ResponseEntity<Object> UserBadRequest(NoSongException e){
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",e.getExceptionCode().getStatus());
        response.put("message",e.getExceptionCode().getMessage());
        return new ResponseEntity<>(response,e.getExceptionCode().getStatus());
    }
//    @ExceptionHandler(DjangoRequestException.class)
//    public ResponseEntity<Object> DjangoBadRequest(DjangoRequestException e){
//        //여기서는 usersong status바꾸기\
//        //
//        Map<String,Object> response = new HashMap<>();
//        response
//    }
}
