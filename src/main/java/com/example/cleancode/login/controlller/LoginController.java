package com.example.cleancode.login.controlller;

import com.example.cleancode.login.service.LoginService;
import com.example.cleancode.login.service.oauth.KakaoLoginParam;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;
    @PostMapping("/login")
    public void login(@RequestParam String authorizationCode, HttpServletResponse response){
        System.out.println("loginService = " + loginService);
        System.out.println("authorizationCode = " + authorizationCode);
        KakaoLoginParam kakaoLoginParam = new KakaoLoginParam();
        kakaoLoginParam.setAuthorizationCode(authorizationCode);
        loginService.steps(kakaoLoginParam, response);
    }

    @GetMapping("/test")
    public @ResponseBody String test(){
        return "hello";
    }
}
