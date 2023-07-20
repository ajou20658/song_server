package com.example.cleancode.api.service.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;


import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final Long tokenMillisecond;
    private final Long refreshMillisecond;

    public static final String BEARER_PREFIX = "Bearer ";

    public JwtService(
            @Value("${jwt.secret-key}")final String secretKey,
            @Value("${jwt.token.expiration-time}")final Long time,
            @Value("${jwt.token.refresh-expiration-time}")final Long refreshTime
    ){
        this.secretKey=Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.tokenMillisecond=time;
        this.refreshMillisecond=refreshTime;
    }

    public String tokenGenerate(String memberEmail){
        final Date now = new Date();
        final Date validity = new Date(now.getTime()+tokenMillisecond);
        return Jwts.builder()
                .setSubject(memberEmail)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey,SignatureAlgorithm.HS256)
                .compact();
    }
    public Jwt generate(String memberEmail){
        String accessToken = tokenGenerate(memberEmail);
        String refreshToken = refreshTokenGenerate(memberEmail);
        return new Jwt(accessToken,refreshToken,"Bearer");
    }
    public String refreshTokenGenerate(String memberEmail){
        final Date now = new Date();
        final Date validity = new Date(now.getTime()+refreshMillisecond);
        return Jwts.builder()
                .setSubject(memberEmail)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey,SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String jwt){
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        }catch (ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public boolean validateToken(String jwt){
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            if(claims.getExpiration().before(new Date())){
                //만료
                return false;
            }
            //유효
            return true;
        }catch (ExpiredJwtException ex){
            //만료
            return false;
        }catch (Exception ex){
            //검증 실패
            return false;
        }
    }
    public String getPayload(String jwt){
        Claims claims = parseClaims(jwt);
        return claims.getSubject();
    }
    public String resolveJwt(String jwt){
        if(StringUtils.hasText(jwt)){
            return jwt;
        }
        return null;
    }

    public Long getExpire(String jwt){
        Date expire = Jwts.parserBuilder().setSigningKey(secretKey)
                .build().parseClaimsJws(jwt)
                .getBody().getExpiration();
        Long now = new Date().getTime();
        return (expire.getTime() - now);
    }

    public Authentication getAuthentication(String jwt){
        Claims claims= parseClaims(jwt);
        String email = claims.getSubject();
        if(email==null){
            throw new RuntimeException("이메일이 저장되지 않은 사용자");
        }
        UserDetails principal = new User(email,"",new ArrayList<>());
        return new UsernamePasswordAuthenticationToken(principal,"",principal.getAuthorities());
    }
}
