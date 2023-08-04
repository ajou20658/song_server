package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Jwt;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class MemberRequest {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MemberRepository memberRepository;

    public MemberDto findMember(HttpServletRequest request){
        Optional<JwtDto> jwtDtoE = jwtTokenProvider.resolveJwt(request);
        if(jwtDtoE.isEmpty()){
            return null;
        }
        JwtDto jwtDto = jwtDtoE.get();
        log.info(jwtDto.getAccessToken(),jwtDto.getRefreshToken());
        Long id = jwtTokenProvider.getId(jwtDto);
        log.info(id.toString());
        Optional<Member> mem = memberRepository.findById(id);
        Member member = mem.get();
        if(mem.isEmpty()) return null;
        return MemberDto.builder()
                .email(member.getEmail())
                .id(member.getId())
                .preferences(member.getPreferences())
                .nickname(member.getNickname())
                .build();
    }
    public Optional<JwtDto> updateJwt(HttpServletRequest request){
        Optional<JwtDto> jwtDtoE = jwtTokenProvider.resolveJwt(request);
        if(jwtDtoE.isEmpty()){
            return Optional.empty();
        }
        JwtDto jwtDto = jwtDtoE.get();
        boolean OK = jwtTokenProvider.validateRefresh(jwtDto);
        if(OK = true){//Collections.singletonList(Role.ROLE_USER)
            return Optional.of(jwtTokenProvider.generate(jwtTokenProvider.getId(jwtDto), Collections.singletonList(jwtTokenProvider.getRole(jwtDto))));
        }else{
            return Optional.empty();
        }
    }
//    public boolean updatePrefer(HttpServletRequest request){
//        request.get
//    }

}
