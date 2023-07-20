package com.example.cleancode.api.service.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Jwt {
    private String accessJwt;
    private String refreshJwt;
    private String grantType;
}
