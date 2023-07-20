package com.example.cleancode.api.service.oauth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
@Getter
@Setter
@RequiredArgsConstructor
public class KakaoLoginParams {
    private static final String GRANT_TYPE = "authorization_code";
    public String authorizationCode;
    @Value("${oauth.kakao.client-id}")
    private static String clientId;

    public MultiValueMap<String,String> makeBody(){
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("code",this.authorizationCode);

        return body;
    }
}
