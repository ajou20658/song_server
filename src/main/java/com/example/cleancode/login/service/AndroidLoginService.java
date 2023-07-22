package com.example.cleancode.login.service;

import com.example.cleancode.login.JpaRepository.MemberRepository;
import com.example.cleancode.login.JpaRepository.TokenRepository;
import com.example.cleancode.login.entity.KakaoToken;
import com.example.cleancode.login.entity.Member;
import com.example.cleancode.login.entity.Role;
import com.example.cleancode.login.service.oauth.*;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AndroidLoginService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private KakaoTokenProvider kakaoTokenProvider;

    //로그인은 앱에서 맡기고 이후 요청은 암호화된 id값으로
    //여기서는 멤버객체 추가만 구현
    public void join(AndroidRequestParam androidRequestParam){
        System.out.println("memberRepository = " + memberRepository);
        System.out.println("kakaoTokenProvider = " + kakaoTokenProvider);
        System.out.println("androidRequestParam = " + androidRequestParam);
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenProvider.tokenInfo(androidRequestParam.getAccessToken());
        Long id = kakaoValidateResponse.getId();
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenProvider.requestUserInfo(androidRequestParam.getAccessToken());
        Optional<Member> isExist = memberRepository.findById(id);
        if(isExist.isEmpty()){
            Member member = Member.builder()
                    .id(id)
                    .email(kakaoInfoResponse.getKakaoAccount().getEmail())
                    .nickname(kakaoInfoResponse.getKakaoAccount().getProfile().getNickname())
                    .role(Role.ROLE_USER)
                    .build();
            log.info(member.toString());
            memberRepository.save(member);
            tokenRepository.save(
                KakaoToken.builder()
                .id(id)
                .token(androidRequestParam.getAccessToken()).refreshToken(androidRequestParam.getRefreshToken())
                .expire(Long.parseLong(androidRequestParam.getAccessTokenExpiresAt())).refreshExpire(Long.parseLong(androidRequestParam.getRefreshTokenExpiresAt()))
                .scope(androidRequestParam.getScopes())
                .build()
            );
        }
    }
}
