package com.example.cleancode.login.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class KakaoValidateResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("expires_in")
    private Long expires_in;
    @JsonProperty("app_id")
    private Long app_id;
}
