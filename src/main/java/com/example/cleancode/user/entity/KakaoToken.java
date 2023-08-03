package com.example.cleancode.user.entity;

import com.example.cleancode.user.dto.KakaoTokenDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoToken {
    @Id
    private Long id;
    private String token;
    private String refreshToken;
    private Long expire;
    private Long refreshExpire;
    private String scope;

    public KakaoTokenDto toDto(){
        return KakaoTokenDto.builder()
                .id(id)
                .token(token)
                .refreshToken(refreshToken)
                .expire(expire)
                .refreshExpire(refreshExpire)
                .build();
    }
}
