package com.example.cleancode.login.service.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
@Component
@Getter
@Setter
public class KakaoLoginParam {
    private String authorizationCode;

    public MultiValueMap<String,String> makeBody(){
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("code",this.authorizationCode);
        return body;
    }
}
