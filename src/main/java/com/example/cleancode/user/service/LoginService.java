package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.service.oauth.KakaoInfoResponse;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.utils.jwt.JwtService;
import com.example.cleancode.user.service.oauth.KakaoLoginParam;
import com.example.cleancode.user.service.oauth.KakaoTokenProvider;
import com.example.cleancode.user.service.oauth.KakaoTokenResponse;
import com.example.cleancode.user.service.oauth.KakaoValidateResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class LoginService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private KakaoTokenProvider kakaoTokenProvider;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${jwt.token.expiration-time}")
    private Long tokenMillisecond;
    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshMillisecond;


    //jwt 토큰이 없거나 만료된 유저들
    @Transactional
    public JwtDto join(KakaoLoginParam kakaoLoginParam) throws Exception {
        KakaoTokenResponse kakaoTokenResponse = kakaoTokenProvider.requestAccessToken(kakaoLoginParam);
        //1. authorizationCode 로 카카오톡 accesstoken과 refreshtoken받아오기
        //1-2 토큰 유효성 검사 + 회원번호 획득
        if(kakaoTokenResponse==null){
            log.info("받은 액세스 토큰 : {}",kakaoLoginParam.getAuthorizationCode());
            log.error("유효하지않은 카카오 accessToken");
            throw new Exception();
        }
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenProvider.tokenInfo(kakaoTokenResponse.getAccessToken());
        Long id = kakaoValidateResponse.getId();
        //2. 받아온 accesstoken이용하여 사용자 정보 요청 & 받아오기
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenProvider.requestUserInfo(kakaoTokenResponse.getAccessToken());
        Optional<Member> isExist = memberRepository.findByid(id);
        //회원정보 저장 필요
        //사용자 추가
        if(isExist.isEmpty()) {
            Member member = Member.builder()
                    .id(id)
                    .email(kakaoInfoResponse.getKakaoAccount().getEmail())
                    .nickname(kakaoInfoResponse.getKakaoAccount().getProfile().getNickname())
                    .profile(kakaoInfoResponse.getKakaoAccount().getProfile().getThumbnail_image_url())
                    .role(Role.ROLE_USER)
                    .build();
            log.info(member.toString());
            memberRepository.save(member);

            return jwtService.generate(member.toMemberDto());
        }
        return jwtService.generate(isExist.get().toMemberDto());
    }
    public boolean logout(HttpServletRequest request){
        try{
            JwtDto jwtDto= jwtService.resolveJwt(request).get();
            Long expiration = jwtService.getExpiration(jwtDto);
            redisTemplate.opsForValue().set(jwtDto.getAccessToken(),"logout",expiration);
            return true;
        }catch (Exception ex){
            log.error("블랙리스트 등록 에러");
            return false;
        }
    }
    public JwtDto login(KakaoLoginParam kakaoLoginParam, HttpServletResponse response) {
        try{
            KakaoTokenResponse KResponse = kakaoTokenProvider.requestAccessToken(kakaoLoginParam);
            String accessToken = KResponse.getAccessToken();
            String refreshToken = KResponse.getRefreshToken();
            KakaoValidateResponse validateResponse = kakaoTokenProvider.tokenInfo(accessToken);
            Long id = validateResponse.getId();
            Optional<Member> member = memberRepository.findById(id);
            MemberDto memberDto = member.get().toMemberDto();

            return jwtService.generate(memberDto);
        }catch (Exception e){
            System.out.println("Transaction rolled back:{}"+e.getMessage());
            return null;
        }
    }
}
