package com.example.cleancode.utils.jwt;

import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.utils.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {


    String secretKey = "ad861fbe42d6d4517997caaac85748c7b33ac43b0eb3d6fe19126eba998c1bb5c29f6110dd5e5d45c287462bc2cbd90865485e93e313717260b3a054c9d355a7";

    Long tokenMillisecond =2592000l;

    Long refreshMillisecond=2592000l;
    @Test
    @Disabled
    void generate() {
        UserDto memberDto = new UserDto();
        List<Role> roles = Collections.singletonList(Role.ROLE_USER);
    }

    @Test
    void refresh() {
    }
    @Test
    void generateToken() {
        UserDto memberDto = UserDto.builder()
                .profileUrl("이것은 프로필")
                .email("이것은 이메일")
                .nickname("이것은 닉네임")
                .id(12345l)
                .role(Role.ROLE_USER)
                .build();
        Date now = new Date();
        Date expirationDate = new Date(now.getTime()+tokenMillisecond*1000l);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        Claims claims = Jwts.claims().setSubject(memberDto.getId().toString());
        claims.put("roles",memberDto.getRole());
        claims.put("email",memberDto.getEmail());
        claims.put("profile",memberDto.getProfileUrl());
        claims.put("nickname",memberDto.getNickname());
        String token=null;
        try {
            token = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expirationDate)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        System.out.println("token = "+token);
        String result = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOTE5MjkzIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImVtYWlsIjoia3d5MTM3OUBuYXZlci5jb20iLCJwcm9maWxlIjoia3d5MTM3OSIsIm5pY2tuYW1lIjoia3d5MTM3OSIsImlhdCI6MTY5MzYzNzk5MCwiZXhwIjoxNjk2MjI5OTkwfQ.Nh33Nm4YoRpbzNF3T3f-pElitkr7S-6hbuAU_G6Bozc";
        assertTrue(token instanceof String);
    }
    @Test
    void validateToken() {
    }

    @Test
    void validateRefresh() {
    }

    @Test
    void getId() {

    }

    @Test
    void getClaim() {
    }

    @Test
    void getExpiration() {
    }

    @Test
    void resolveJwt() {
    }

    @Test
    void authenticate() {
    }
}