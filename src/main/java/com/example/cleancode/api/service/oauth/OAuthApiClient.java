package com.example.cleancode.api.service.oauth;

import com.example.cleancode.api.entity.KakaoInfoResponse;
import com.example.cleancode.api.entity.KakaoTokenResponse;
import com.example.cleancode.api.entity.OAuthResponse;
import com.example.cleancode.api.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthApiClient {
    public static final String GRANT_TYPE = "authorization_code";
    @Value("${oauth.kakao.url.auth}")
    private String authUrl;
    @Value("${oauth.kakao.url.api}")
    private String apiUrl;
    @Value("${oauth.kakao.client-id}")
    private String clientId;
    @Autowired
    private final WebClient webClient;

    public OAuthResponse requestAccessToken(KakaoLoginParams params) {
        String url = authUrl + "/oauth/token";
        MultiValueMap<String, String> body = params.makeBody();
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .map(kakaoTokenResponse -> new OAuthResponse(
                        kakaoTokenResponse.getAccessToken(),
                        kakaoTokenResponse.getRefreshToken(),
                        Long.parseLong(kakaoTokenResponse.getExpiresIn()),
                        Long.parseLong(kakaoTokenResponse.getRefreshTokenExpiresIn()),
                        kakaoTokenResponse.getScope()
                ))
                .block();
    }

    public KakaoInfoResponse requestUserInfo(OAuthResponse accessToken) {
        String url = authUrl + "/v2/user/me";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("property_keys", "[\"kakao_account.email\", \"kakao_account.profile\"]");
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Bearer " + accessToken.getAccess_token())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(KakaoInfoResponse.class).block();
    }
}
