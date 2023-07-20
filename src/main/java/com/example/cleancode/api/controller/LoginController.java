package com.example.cleancode.api.controller;

import com.example.cleancode.api.entity.User;
import com.example.cleancode.api.entity.UserRepository;
import com.example.cleancode.api.service.jwt.Jwt;
import com.example.cleancode.api.service.jwt.JwtService;
import com.example.cleancode.api.service.oauth.KakaoLoginParams;
import com.example.cleancode.api.service.oauth.OAuthLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    @Autowired
    private OAuthLoginService oAuthLoginService;
    @PostMapping("/login")
    public void oauthlogin(@RequestParam String authorizationCode){
        KakaoLoginParams params = new KakaoLoginParams();
        params.setAuthorizationCode(authorizationCode);
        log.info(params.toString());
        log.info("authorizationCode: "+ authorizationCode);
        Jwt jwt = oAuthLoginService.login(params);
        String access = jwt.getAccessJwt();
        String refresh = jwt.getRefreshJwt();
//        Cookie jwtCookie = new Cookie("accessToken",access);
//        Cookie refreshCookie = new Cookie("refreshToken",refresh);
        log.info("accessToken: "+access);
        log.info("refreshToken: "+refresh);
//        response.addCookie(jwtCookie);
//        response.addCookie(refreshCookie);
//        return ResponseEntity.ok().body(jwt);
    }

//    @PostMapping("/logout")
//    public ResponseEntity<?> oauthlogout(){
//
//    }
    @GetMapping("/members")
    public ResponseEntity<List<User>> findAll(){
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/members/{accessToken}") //jwt토큰으로 Member entity 반환
    public ResponseEntity<User> findByJwt(@PathVariable String jwtToken){
        String memberEmail = jwtService.getPayload(jwtToken);//유저 이메일
        return ResponseEntity.ok(userRepository.findByEmail(memberEmail).get());
    }
    @GetMapping("/test")
    public @ResponseBody String test(){
        return "hello";
    }

}
