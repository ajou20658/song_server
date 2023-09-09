package com.example.cleancode.user.service.oauth;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public class authRequestParam {
    private String authorizationCode;
}
