package com.example.cleancode.user.controller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.UserPrinciple;
import com.example.cleancode.user.service.AndroidLoginService;
import com.example.cleancode.user.service.oauth.AndroidRequestParam;
import com.example.cleancode.utils.ApiResponseJson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ApiResponseJson login(@RequestBody AndroidRequestParam androidRequestParam){
        JwtDto jwtDto = androidLoginService.join(androidRequestParam);
        log.info("Token Issued : {}",jwtDto);
        return new ApiResponseJson(HttpStatus.OK,jwtDto);
    }

//    @GetMapping("/member")
//    public ApiResponseJson member(@AuthenticationPrincipal UserPrinciple userPrinciple){
//        Long id = Long.valueOf(userPrinciple.getId());
//        Member member =  memberRepository.findById(id).orElse(null);
//        if(member==null){
//            return new ApiResponseJson(HttpStatus.NOT_FOUND, null);
//        }
//        return new ApiResponseJson(HttpStatus.OK,member);
//    }
//
//    @PostMapping("/update_prefer")
//    public ApiResponseJson preferUpdate(@RequestBody List<String> artist, @RequestBody List<String> genre, @RequestBody List<String> title, @AuthenticationPrincipal UserPrinciple userPrinciple){
//        Member memberE = memberRepository.findById(Long.parseLong(userPrinciple.getId())).get();
//        MemberDto member = memberE.toMemberDto();
//        Set<String> set_artist = new HashSet<>(artist);
//        Set<String> set_genre = new HashSet<>(genre);
//        Set<String> set_title = new HashSet<>(title);
//        member.setPreference_Genre(set_genre);
//        member.setPreference_Singer(set_artist);
//        member.setPreference_Title(set_title);
//        try {
//            Member result = memberRepository.save(member.makeMember());
//            log.info("유저 선호도 업데이트 : {}",result);
//            return new ApiResponseJson(HttpStatus.OK,result);
//        }catch(IllegalArgumentException ex){
//            log.info("유저 선호도 업뎃 실패(IllegalArgu) {}",member);
//            return new ApiResponseJson(HttpStatus.BAD_REQUEST,null);
//        }catch(OptimisticLockingFailureException ex){
//            log.info("유저 선호도 업뎃 실패(낙관적 Locking) {}",member);
//            return new ApiResponseJson(HttpStatus.CONFLICT,null);
//        }
//
//    }
}
