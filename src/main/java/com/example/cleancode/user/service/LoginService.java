package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.service.oauth.KakaoInfoResponse;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.utils.Role;
import com.example.cleancode.utils.jwt.JwtService;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.user.service.oauth.KakaoTokenService;
import com.example.cleancode.user.service.oauth.KakaoTokenResponse;
import com.example.cleancode.user.service.oauth.KakaoValidateResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository memberRepository;
    private final KakaoTokenService kakaoTokenService;
    private final JwtService jwtService;
    private final RedisTemplate<String,Object> redisTemplate;
    @Value("${jwt.token.expiration-time}")
    private Long tokenMillisecond;
    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshMillisecond;


    //jwt 토큰이 없거나 만료된 유저들
    @Transactional
    public JwtDto join(KakaoLoginParam kakaoLoginParam) throws Exception {
        KakaoTokenResponse kakaoTokenResponse = kakaoTokenService.requestAccessToken(kakaoLoginParam);
        //1. authorizationCode 로 카카오톡 accesstoken과 refreshtoken받아오기
        //1-2 토큰 유효성 검사 + 회원번호 획득
        if(kakaoTokenResponse.getAccessToken()==null){
            log.info("받은 액세스 토큰 : {}",kakaoLoginParam.getAuthorizationCode());
            log.error("유효하지않은 카카오 accessToken");
            throw new Exception();
        }
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenService.tokenInfo(kakaoTokenResponse.getAccessToken());
        Long id = kakaoValidateResponse.getId();
        //2. 받아온 accesstoken이용하여 사용자 정보 요청 & 받아오기
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenService.requestUserInfo(kakaoTokenResponse.getAccessToken());
//        kakaoTokenService.tokenfire(kakaoTokenResponse.getAccessToken(),id);
        Optional<User> isExist = memberRepository.findById(id);
        //회원정보 저장 필요
        //사용자 추가
        if(isExist.isEmpty()) {
            User member = User.builder()
                    .id(id)
                    .email(kakaoInfoResponse.getKakaoAccount().getEmail())
                    .nickname(kakaoInfoResponse.getKakaoAccount().getProfile().getNickname())
                    .profileUrl(kakaoInfoResponse.getKakaoAccount().getProfile().getThumbnail_image_url())
                    .role(Role.ROLE_USER)
                    .build();
            log.info(member.toString());
            memberRepository.save(member);

            return jwtService.generate(member.toMemberDto());
        }
        return jwtService.generate(isExist.get().toMemberDto());
    }

    public JwtDto login(KakaoLoginParam kakaoLoginParam, HttpServletResponse response) {
        try{
            KakaoTokenResponse KResponse = kakaoTokenService.requestAccessToken(kakaoLoginParam);
            String accessToken = KResponse.getAccessToken();
            String refreshToken = KResponse.getRefreshToken();
            KakaoValidateResponse validateResponse = kakaoTokenService.tokenInfo(accessToken);
            Long id = validateResponse.getId();
            Optional<User> member = memberRepository.findById(id);
            UserDto memberDto = member.get().toMemberDto();

            return jwtService.generate(memberDto);
        }catch (Exception e){
            System.out.println("Transaction rolled back:{}"+e.getMessage());
            return null;
        }
    }
}
