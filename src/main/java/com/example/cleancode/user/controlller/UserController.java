package com.example.cleancode.user.controlller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Jwt;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.service.LoginService;
import com.example.cleancode.user.service.MemberRequest;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequestMapping("/v2")
public class UserController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private MemberRepository memberRepository;
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

    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     * @param request
     * @return
     */
    @GetMapping("/member")
    public ResponseEntity<MemberDto> memberinfo(HttpServletRequest request){
        MemberDto member = memberRequest.findMember(request);
        log.info(member.toString());
        return ResponseEntity.ok(member);
    }

    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 검색후 유저 선호 장르 업데이트
     * @param artist
     * @param genre
     * @param title
     * @param request
     */
    @PostMapping("/update_prefer")
    public void preferUpdate(@RequestBody List<String> artist,@RequestBody List<String> genre,@RequestBody List<String> title,HttpServletRequest request){
        MemberDto member = memberRequest.findMember(request);
        Set<String> set_artist = new HashSet<>(artist);
        Set<String> set_genre = new HashSet<>(genre);
        Set<String> set_title = new HashSet<>(title);
        member.setPreference_Genre(set_genre);
        member.setPreference_Singer(set_artist);
        member.setPreference_Title(set_title);
        memberRepository.save(member.makeMember());
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
    @PostMapping("/jwtUpdate")
    public void jwtUpdate(HttpServletRequest request, HttpServletResponse response){
        Optional<JwtDto> jwtDtoE = memberRequest.updateJwt(request);
        if(jwtDtoE.isEmpty()){
            return;
        }
        JwtDto jwtDto = jwtDtoE.get();
        loginService.setCookie(response,jwtDto);
    }


    @GetMapping("/test")
    public @ResponseBody String test(){
        return "hello";
    }
}
