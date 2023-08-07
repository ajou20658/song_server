package com.example.cleancode.user.controlller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.service.LoginService;
import com.example.cleancode.user.service.MemberRequest;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@Slf4j
@RequestMapping("/v2")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberRequest memberRequest;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public void login(@RequestParam String authorizationCode, HttpServletResponse response){
        System.out.println("loginService = " + loginService);
        System.out.println("authorizationCode = " + authorizationCode);
        KakaoLoginParam kakaoLoginParam = new KakaoLoginParam();
        kakaoLoginParam.setAuthorizationCode(authorizationCode);
        loginService.steps(kakaoLoginParam, response);
    }
    /**
     * 멤버정보 업데이트
     * 필요한 json email,nickname,preferences
     * @param memberDto
     * @param request
     */
    @PostMapping("/update_user")
    public void userUpdate(@RequestBody MemberDto memberDto,HttpServletRequest request){
        MemberDto member = memberRequest.findMember(request);
        memberDto.setId(member.getId());
        memberDto.setRole(Role.ROLE_USER);
        memberRepository.save(memberDto.makeMember());
    }

    /**
     * 유저 jwt 만료 토큰 업데이트
     * @param request
     * @param response
     */
    @PostMapping("/jwtUpdate")
    public void jwtUpdate(HttpServletRequest request, HttpServletResponse response){
        Optional<JwtDto> jwtDtoE = memberRequest.updateJwt(request);
        if(jwtDtoE.isEmpty()){

            return;
        }
        JwtDto jwtDto = jwtDtoE.get();
        loginService.setCookie(response,jwtDto);
        loginService.forTest(jwtDto,jwtTokenProvider.getId(jwtDto));
    }

    @GetMapping("/test")
    public @ResponseBody String test(){
        return "hello";
    }
}
