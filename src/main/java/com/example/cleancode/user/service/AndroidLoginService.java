package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.utils.Role;
import com.example.cleancode.user.service.oauth.*;
import com.example.cleancode.utils.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AndroidLoginService {
    private final UserRepository memberRepository;
    private final KakaoTokenService kakaoTokenService;
    private final JwtService jwtService;

    //로그인은 앱에서 맡기고 이후 요청은 암호화된 id값으로
    //여기서는 멤버객체 추가만 구현
    @Transactional
    public JwtDto join(AndroidRequestParam androidRequestParam){
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenService.tokenInfo(androidRequestParam.getAccessToken());
        System.out.println(kakaoValidateResponse);
        Long id = kakaoValidateResponse.getId();
        //2. 받아온 accesstoken이용하여 사용자 정보 요청 & 받아오기
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenService.requestUserInfo(androidRequestParam.getAccessToken());
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
}
