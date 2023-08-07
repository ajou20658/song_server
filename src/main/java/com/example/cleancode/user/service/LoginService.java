package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.JwtRepository;
import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.JpaRepository.TokenRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.entity.Jwt;
import com.example.cleancode.user.service.oauth.KakaoInfoResponse;
import com.example.cleancode.user.entity.KakaoToken;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.utils.jwt.JwtTokenProvider;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.user.service.oauth.KakaoTokenProvider;
import com.example.cleancode.user.service.oauth.KakaoTokenResponse;
import com.example.cleancode.user.service.oauth.KakaoValidateResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
public class LoginService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired//Only For Test
    private JwtRepository jwtRepository;
    @Autowired
    private KakaoTokenProvider kakaoTokenProvider;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.token.expiration-time}")
    private Long tokenMillisecond;
    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshMillisecond;

    //jwt 토큰이 없거나 만료된 유저들
    public void steps(KakaoLoginParam kakaoLoginParam, HttpServletResponse response){
        //1. authorizationCode 로 카카오톡 accesstoken과 refreshtoken받아오기
        KakaoTokenResponse kakaoTokenResponse = kakaoTokenProvider.requestAccessToken(kakaoLoginParam);
        if(kakaoTokenResponse == null){
            //예외 코드 추가 필요
            return;
        }
        //1-2 토큰 유효성 검사 + 회원번호 획득
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenProvider.tokenInfo(kakaoTokenResponse.getAccessToken());
        Long id = kakaoValidateResponse.getId();
        //2. 받아온 accesstoken이용하여 사용자 정보 요청 & 받아오기
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenProvider.requestUserInfo(kakaoTokenResponse.getAccessToken());
        Optional<Member> isExist = memberRepository.findById(id);

        //3-1. 유저DB에 일치하는 사람이 없을 경우
        if(isExist.isEmpty()){
            //사용자 추가
            Member member = Member.builder()
                    .id(id)
                    .email(kakaoInfoResponse.getKakaoAccount().getEmail())
                    .nickname(kakaoInfoResponse.getKakaoAccount().getProfile().getNickname())
                    .role(Role.ROLE_USER)
                    .build();
            log.info(member.toString());
            memberRepository.save(member);

            //토큰 저장
            tokenRepository
                    .save(KakaoToken.builder()
                    .id(id)//회원번호 받아오기
                    .token(kakaoTokenResponse.getAccessToken()).refreshToken(kakaoTokenResponse.getRefreshToken())
                    .expire(Long.parseLong(kakaoTokenResponse.getExpiresIn())).refreshExpire(Long.parseLong(kakaoTokenResponse.getRefreshTokenExpiresIn()))
                    .scope(kakaoTokenResponse.getScope())
                    .build());
            JwtDto jwtDto = jwtTokenProvider.generate(id,Collections.singletonList(Role.ROLE_USER));
            System.out.println("토큰 발급완료");
            setCookie(response,jwtDto);
            //-------------------테스트 용도
            forTest(jwtDto,member);
            //-------------------테스트 용도
//사용자가 있다면
        }else{
            JwtDto jwtDto = jwtTokenProvider.generate(id,Collections.singletonList(Role.ROLE_USER));
            System.out.println("토큰 발급완료");
            setCookie(response,jwtDto);
            //-------------------테스트 용도
            forTest(jwtDto,isExist.get());
            //-------------------테스트 용도
        }
        //jwt토큰 사용자 쿠키에 저장

    }
    public void setCookie(HttpServletResponse response, JwtDto jwtDto){
        Cookie jwtCookie = new Cookie("jwtCookie",jwtDto.getAccessToken());
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(Math.toIntExact(tokenMillisecond));
        response.addCookie(jwtCookie);
        Cookie jwtRefresh = new Cookie("jwtRefresh",jwtDto.getRefreshToken());
        jwtRefresh.setPath("/");
        jwtRefresh.setMaxAge(Math.toIntExact(refreshMillisecond));
        response.addCookie(jwtRefresh);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"); // 클라이언트 도메인 설정
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"); // CORS 허용 설정
    }
    public void forTest(JwtDto jwtDto,Member member){
        Jwt jwt = Jwt.builder()
                .accessToken(jwtDto.getAccessToken())
                .name(member.getNickname())
                .email(member.getEmail())
                .refreshToken(jwtDto.getRefreshToken())
                .id(member.getId())
                .build();
        log.info(jwt.toString());
        jwtRepository.save(jwt);
    }
    public void forTest(JwtDto jwtDto,Long id){
        Member member = memberRepository.findById(id).get();
        Jwt jwt = Jwt.builder()
                .accessToken(jwtDto.getAccessToken())
                .name(member.getNickname())
                .email(member.getEmail())
                .refreshToken(jwtDto.getRefreshToken())
                .id(member.getId())
                .build();
        log.info(jwt.toString());
        jwtRepository.save(jwt);
    }
}
