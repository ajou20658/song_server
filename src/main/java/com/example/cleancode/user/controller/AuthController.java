package com.example.cleancode.user.controller;

import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.service.LoginService;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.utils.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/login")
    @ResponseBody
    public JwtDto login(@RequestParam String authorizationCode, HttpServletResponse response){
        KakaoLoginParam kakaoLoginParam = new KakaoLoginParam();
        kakaoLoginParam.setAuthorizationCode(authorizationCode);
        try {

            return loginService.join(kakaoLoginParam);
        }catch (Exception ex){
            log.info("Exception");
            ex.printStackTrace();
            return null;
        }
    }
    /**
     * 유저 jwt 갱신
     * @param request
     * @param response null이면 로그인창으로 보내기, null이 아니면 제대로 갱신된 것
     */
    @PostMapping("/jwtUpdate")
    @ResponseBody
    public JwtDto jwtUpdate(HttpServletRequest request, HttpServletResponse response){
        Optional<JwtDto> jwtDtoE = jwtService.resolveJwt(request);
        if(jwtDtoE.isEmpty()) return null;
        return jwtService.refresh(jwtDtoE.get());
    }

    @PostMapping("/logout")
    public boolean logout(HttpServletRequest request){
        //redis blacklist에 유효기간 만큼 저장
        return loginService.logout(request);
    }
}
