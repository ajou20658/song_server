package com.example.cleancode.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoInfoResponse implements InfoResponse{
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KakaoAccount {
        private KakaoProfile profile;
        private String email;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class KakaoProfile {
        private String nickname;
    }
    @Override
    public String getEmail(){
        return kakaoAccount.email;
    }
    @Override
    public String getNickname(){
        return kakaoAccount.profile.nickname;
    }
}
