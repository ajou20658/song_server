package com.example.cleancode.user.controller;

import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.service.LoginService;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.utils.ApiResponseJson;
import com.example.cleancode.utils.jwt.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Object> login(@RequestBody KakaoLoginParam kakaoLoginParam){

        try {
            JwtDto jwtDto = loginService.join(kakaoLoginParam);
            log.info("Token Issued");
            Map<String,Object> response = new HashMap<>();
            response.put("HttpStatus",HttpStatus.OK.value());
            response.put("response",jwtDto);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception ex){
            log.info("Exception");
            ex.printStackTrace();
            Map<String,Object> response = new HashMap<>();
            response.put("HttpStatus",HttpStatus.FORBIDDEN.value());
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/jwtUpdate")
    @ResponseBody
    public ResponseEntity<Object> jwtUpdate(@RequestBody JwtDto jwtDto){

        Map<String,Object> res = new HashMap<>();
        res.put("response",jwtService.refresh(jwtDto));
        return new ResponseEntity<>(res,HttpStatus.OK);

    }

    @PostMapping("/logout")
    public boolean logout(HttpServletRequest request){
        //redis blacklist에 유효기간 만큼 저장
        return loginService.logout(request);
    }
}
