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

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AndroidLoginService {
    private final UserRepository memberRepository;
    private final KakaoTokenService kakaoTokenService;
    private final JwtService jwtService;

    @Transactional
    public JwtDto join(AndroidRequestParam androidRequestParam){
        KakaoValidateResponse kakaoValidateResponse = kakaoTokenService.tokenInfo(androidRequestParam.getAccessToken());
        System.out.println(kakaoValidateResponse);
        Long id = kakaoValidateResponse.getId();
        System.out.println("id = " + id);
        KakaoInfoResponse kakaoInfoResponse = kakaoTokenService.requestUserInfo(androidRequestParam.getAccessToken());
        kakaoTokenService.tokenfire(androidRequestParam.getAccessToken(), id);
        Optional<User> isExist = memberRepository.findById(id);
        if(isExist.isEmpty()) {
            List<Integer> integerList = List.of(100,150,200,250,300,350,400,450);
            User member = User.builder()
                    .id(id)
                    .email(kakaoInfoResponse.getKakaoAccount().getEmail())
                    .nickname(kakaoInfoResponse.getKakaoAccount().getProfile().getNickname())
                    .profileUrl(kakaoInfoResponse.getKakaoAccount().getProfile().getThumbnail_image_url())
                    .spectr(integerList)
                    .role(Role.ROLE_USER)
                    .build();
            log.info(member.toString());
            memberRepository.save(member);

            return jwtService.generate(member.toMemberDto());
        }
        return jwtService.generate(isExist.get().toMemberDto());
    }
}
