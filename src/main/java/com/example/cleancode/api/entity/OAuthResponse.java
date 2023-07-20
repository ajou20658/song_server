package com.example.cleancode.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class OAuthResponse {
    private String access_token;
    private String refresh_token;
    private Long expires_in;
    private Long refresh_token_expires_in;
    private String token_type;
}
