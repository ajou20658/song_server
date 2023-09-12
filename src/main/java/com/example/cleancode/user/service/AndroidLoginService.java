package com.example.cleancode.user.service;

import com.beust.ah.A;
import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.service.oauth.*;
import com.example.cleancode.utils.ApiResponseJson;
import com.example.cleancode.utils.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class AndroidLoginService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private KakaoTokenProvider kakaoTokenProvider;
    @Autowired
    private JwtService jwtService;

    //로그인은 앱에서 맡기고 이후 요청은 암호화된 id값으로
    //여기서는 멤버객체 추가만 구현
    @Transactional
    public JwtDto join(AndroidRequestParam androidRequestParam){
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenProvider.tokenInfo(androidRequestParam.getAccessToken());
        System.out.println(kakaoValidateResponse);
        Long id = kakaoValidateResponse.getId();
        //2. 받아온 accesstoken이용하여 사용자 정보 요청 & 받아오기
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenProvider.requestUserInfo(androidRequestParam.getAccessToken());
        Optional<Member> isExist = memberRepository.findByid(id);
        //회원정보 저장 필요
        //사용자 추가
        if(isExist.isEmpty()) {
            Member member = Member.builder()
                    .id(id)
                    .email(kakaoInfoResponse.getKakaoAccount().getEmail())
                    .nickname(kakaoInfoResponse.getKakaoAccount().getProfile().getNickname())
                    .profile(kakaoInfoResponse.getKakaoAccount().getProfile().getProfile())
                    .role(Role.ROLE_USER)
                    .build();
            log.info(member.toString());
            memberRepository.save(member);

            return jwtService.generate(member.toMemberDto());
        }
        return jwtService.generate(isExist.get().toMemberDto());
    }
}
