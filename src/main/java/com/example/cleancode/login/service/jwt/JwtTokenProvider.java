package com.example.cleancode.login.service.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.atn.LL1Analyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.token.expiration-time}")
    private Long tokenMillisecond;
    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshMillisecond;


    public final String BEARER_PREFIX = "Bearer ";
    public String generateToken(Long id){
        Date now = new Date();
        Date expirationDate= new Date(now.getTime()+tokenMillisecond*1000l);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String token = Jwts.builder()
                .setSubject(id.toString())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
        return token;
    }
    public String generateRefreshToken(Long id){
        Date now = new Date();
        Date expirationDate= new Date(now.getTime()+refreshMillisecond*1000l);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        String token = Jwts.builder()
                .setSubject(id.toString())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
        return token;
    }
    //토큰 형식만 검증
    public boolean validateToken(String token){
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            if(claims.getExpiration().before(new Date())){
                return false;
            }
            return true;
        }catch (ExpiredJwtException ex){
            return false;
        }catch(Exception ex){
            return false;
        }
    }

    public Long getClaim(String jwt){
        if(validateToken(jwt)){
            try{
                log.info(jwt);
                Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
                String idString = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody().getSubject();
                log.info(idString);
                Long id = Long.parseLong(idString);
                return id;
            }catch(Exception ex){
                log.error("Failed to parse The JWT token.", ex);
                return null;
            }
        }
        return null;
    }

}
