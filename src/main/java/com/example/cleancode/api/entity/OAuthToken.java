package com.example.cleancode.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuthToken {
    @Id
    private String token;
    private String refreshToken;
    private Long expire;
    private Long refreshExpire;
    private String scope;
    private String email;
}
