package com.example.cleancode.login.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class KakaoInfoResponse {
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    public static class KakaoAccount{
        boolean profile_nickname_needs_agreement;
        profile profile;
        String email;
    }
    @Getter
    public static class profile{
        String nickname;
    }

}
