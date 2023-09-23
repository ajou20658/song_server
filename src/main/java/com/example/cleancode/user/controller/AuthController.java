package com.example.cleancode.user.controller;

import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.service.LoginService;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.user.service.oauth.KakaoTokenService;
import com.example.cleancode.utils.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;
    private final JwtService jwtService;
    private final KakaoTokenService kakaoTokenService;
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


}
