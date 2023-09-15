package com.example.cleancode.user.controller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.service.AndroidLoginService;
import com.example.cleancode.user.service.oauth.AndroidRequestParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//http://3.34.194.47:8080/v1/login
@Slf4j
@RestController
@RequestMapping("/android")
public class AndroidLoginController {

    @Autowired
    private AndroidLoginService androidLoginService;
    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Object> login(@RequestBody AndroidRequestParam androidRequestParam){
        try{
            JwtDto jwtDto = androidLoginService.join(androidRequestParam);
            log.info("Token Issued accessToken = {}",jwtDto.getAccessToken());
            log.info("refreshToken = {}",jwtDto.getRefreshToken());
            Map<String,Object> response = new HashMap<>();
            response.put("HttpStatus",HttpStatus.OK.value());
            response.put("response",jwtDto);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception ex){
            log.info("err");
            Map<String,Object> response = new HashMap<>();
            response.put("HttpStatus",HttpStatus.FORBIDDEN.value());
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        }
    }
}
