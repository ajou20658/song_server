package com.example.cleancode.login.service;

import com.example.cleancode.login.JpaRepository.MemberRepository;
import com.example.cleancode.login.dto.JwtDto;
import com.example.cleancode.login.entity.Member;
import com.example.cleancode.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class MemberRequest {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MemberRepository memberRepository;

    public Optional<Member> findMember(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        String access="";
        String refresh="";
        for(Cookie cookie: cookies){
            if("jwtCookie".equals(cookie.getName())){
                 access = cookie.getValue();
            } else if ("jwtRefresh".equals(cookie.getName())) {
                refresh = cookie.getValue();
            }
        }
        JwtDto jwtDto = new JwtDto(access,refresh);
        Long id = jwtTokenProvider.getId(jwtDto);
        log.info(id.toString());
        return memberRepository.findById(id);
    }

//    public boolean updatePrefer(HttpServletRequest request){
//        request.get
//    }
}
