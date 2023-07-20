package com.example.cleancode.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTokenDto {
    private Long id;
    private String token;
    private String refreshToken;
    private Long expire;
    private Long refreshExpire;
    private String tokenType;
    private String scope;
}
