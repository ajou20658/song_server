package com.example.cleancode.login.service;

import com.example.cleancode.login.JpaRepository.MemberRepository;
import com.example.cleancode.login.JpaRepository.TokenRepository;
import com.example.cleancode.login.dto.KakaoTokenDto;
import com.example.cleancode.login.service.oauth.KakaoInfoResponse;
import com.example.cleancode.login.dto.MemberDto;
import com.example.cleancode.login.entity.KakaoToken;
import com.example.cleancode.login.entity.Member;
import com.example.cleancode.login.entity.Role;
import com.example.cleancode.login.service.jwt.JwtTokenProvider;
import com.example.cleancode.login.service.oauth.KakaoLoginParam;
import com.example.cleancode.login.service.oauth.KakaoTokenProvider;
import com.example.cleancode.login.service.oauth.KakaoTokenResponse;
import com.example.cleancode.login.service.oauth.KakaoValidateResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LoginService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenRepository tokenRepository;
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
        System.out.println("memberRepository = " + memberRepository);
        System.out.println("tokenRepository = " + tokenRepository);
        System.out.println("kakaoTokenProvider = " + kakaoTokenProvider);
        System.out.println("jwtTokenProvider = " + jwtTokenProvider);
        System.out.println("tokenMillisecond = " + tokenMillisecond);
        System.out.println("refreshMillisecond = " + refreshMillisecond);
        System.out.println("kakaoLoginParam = " + kakaoLoginParam);
        //1. authorizationCode 로 카카오톡 accesstoken과 refreshtoken받아오기
        KakaoTokenResponse kakaoTokenResponse = kakaoTokenProvider.requestAccessToken(kakaoLoginParam);
        System.out.println("kakaoTokenResponse access = " + kakaoTokenResponse.getAccessToken());
        System.out.println("kakaoTokenResponse refresh= " + kakaoTokenResponse.getRefreshToken());
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
//사용자가 있다면
        }
        //jwt토큰 사용자 쿠키에 저장
        String jwtToken = jwtTokenProvider.generateToken(id);
        String refreshToken = jwtTokenProvider.generateRefreshToken(id);

        Cookie jwtCookie = new Cookie("jwtCookie",jwtToken);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(Math.toIntExact(tokenMillisecond));
        response.addCookie(jwtCookie);

        Cookie jwtRefresh = new Cookie("jwtRefresh",refreshToken);
        jwtRefresh.setPath("/");
        jwtRefresh.setMaxAge(Math.toIntExact(refreshMillisecond));
        response.addCookie(jwtRefresh);
    }

}
