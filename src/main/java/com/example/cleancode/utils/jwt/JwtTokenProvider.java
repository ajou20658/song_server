package com.example.cleancode.utils.jwt;

import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public JwtDto generate(Long id,List<Role>roles){
        return new JwtDto(generateToken(id,roles),generateRefreshToken(id));
    }
    public String generateToken(Long id, List<Role> roles){
        Date now = new Date();
        Date expirationDate= new Date(now.getTime()+tokenMillisecond*1000l);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        Claims claims = Jwts.claims().setSubject(id.toString());
        claims.put("roles",roles);

        String token = Jwts.builder()
                .setClaims(claims)
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
    public boolean validateToken(JwtDto jwtDto){
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtDto.getAccessToken()).getBody();
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
    public boolean validateRefresh(JwtDto jwtDto){
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwtDto.getRefreshToken()).getBody();
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
    public Long getId(JwtDto jwtDto){
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtDto.getAccessToken())
                    .getBody();
            String userId = claims.getSubject();
            return Long.parseLong(userId);
        }
        catch(Exception ex){
            log.info("getId err");
            return null;
        }
    }
    public Role getRole(JwtDto jwtDto){
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtDto.getAccessToken())
                    .getBody();
            ArrayList<Role> a =  claims.get("roles", ArrayList.class);
            return a.get(0);
        }catch (Exception ex){
            log.info("getRole err: "+ex.toString());
            return Role.ROLE_ANONYMOUS;
        }
    }
    public Optional<JwtDto> resolveJwt(HttpServletRequest request){
        try{
            Cookie[] cookies = request.getCookies();

            String access="";
            String refresh="";
            for(Cookie cookie: cookies){
                if("jwtCookie".equals(cookie.getName())){
                    access = cookie.getValue();
                } else if ("jwtRefresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                }
            }
            JwtDto jwtDto = new JwtDto(access,refresh);
            return Optional.of(jwtDto);
        }catch(Exception ex){
            log.info("invalid");
            return Optional.empty();
        }
    }
}
