package com.example.cleancode.login.service.oauth;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AndroidRequestParam {
    private String accessToken;
    private String accessTokenExpiresAt;
    private String refreshToken;
    private String refreshTokenExpiresAt;
    private Long idToken;
    private String scopes;
}
