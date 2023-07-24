package com.example.cleancode.login.controlller;

import com.example.cleancode.login.dto.MemberDto;
import com.example.cleancode.login.entity.Member;
import com.example.cleancode.login.service.LoginService;
import com.example.cleancode.login.service.MemberRequest;
import com.example.cleancode.login.service.jwt.JwtTokenProvider;
import com.example.cleancode.login.service.oauth.KakaoLoginParam;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/v2")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @Autowired
    private MemberRequest memberRequest;

    @PostMapping("/login")
    public void login(@RequestParam String authorizationCode, HttpServletResponse response){
        System.out.println("loginService = " + loginService);
        System.out.println("authorizationCode = " + authorizationCode);
        KakaoLoginParam kakaoLoginParam = new KakaoLoginParam();
        kakaoLoginParam.setAuthorizationCode(authorizationCode);
        loginService.steps(kakaoLoginParam, response);
    }

    @GetMapping("/member")
    public ResponseEntity<Member> memberinfo(HttpServletRequest request){
        Optional<Member> member = memberRequest.findMember(request);
        log.info(member.toString());
        return ResponseEntity.ok(member.orElse(null));
    }
    @GetMapping("/test")
    public @ResponseBody String test(){
        return "hello";
    }
}
