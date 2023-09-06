package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.service.oauth.AndroidRequestParam;
import com.example.cleancode.user.service.oauth.KakaoTokenProvider;
import com.example.cleancode.user.service.oauth.KakaoValidateResponse;
import com.example.cleancode.utils.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AndroidLoginServiceTest {
    @Mock
    MemberRepository memberRepository;
    @Mock
    KakaoTokenProvider kakaoTokenProvider;
    @Mock
    JwtService jwtService;
    @InjectMocks
    AndroidLoginService androidLoginService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void join() {
        /*
        given
         */
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(new Member()));
        when(kakaoTokenProvider.tokenInfo(anyString())).thenReturn(new KakaoValidateResponse());

        AndroidRequestParam param = new AndroidRequestParam();
        param.setAccessToken("test_token");
        JwtDto result = androidLoginService.login(param);

        assertNotNull(result);
        /*
        when,then
         */
    }

    @Test
    void login() {
    }
}