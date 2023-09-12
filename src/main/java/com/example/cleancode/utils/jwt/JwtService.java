package com.example.cleancode.utils.jwt;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.entity.UserPrinciple;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.rmi.UnexpectedException;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService{
    @Autowired
    private MemberRepository memberRepository;
    @Value("${jwt.secret-key}")
    private String secretKey;
    @Value("${jwt.token.expiration-time}")
    private Long tokenMillisecond;
    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshMillisecond;

    public final String BEARER_PREFIX = "Bearer ";
    public JwtDto generate(MemberDto memberDto){
        return new JwtDto(generateToken(memberDto),generateRefreshToken(memberDto.getId()));
    }
    public JwtDto refresh(JwtDto jwtDto){
        Long id = getId(jwtDto);
        MemberDto memberDto = memberRepository.findById(id).get().toMemberDto();
        return new JwtDto(generateToken(memberDto),generateRefreshToken(id));
    }
    //유효기간은 기간+현재시각
    public String generateToken(MemberDto memberDto){
        Date now = new Date();
        Date expirationDate= new Date(now.getTime()+tokenMillisecond*1000l);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        Claims claims = Jwts.claims().setSubject(memberDto.getId().toString());
        claims.put("roles",memberDto.getRole());
        claims.put("email",memberDto.getEmail());
        claims.put("profile",memberDto.getProfile());
        claims.put("nickname",memberDto.getNickname());

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
    //토큰 검증 - 필터에서 사용해야됨 -----------------------------------------------------
    public boolean validateToken(JwtDto jwtDto){
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        try{
            log.info("들어옴 : {}",jwtDto.getAccessToken());
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).build();
            log.info("들어옴 : {}");
            Claims claims = jwtParser.parseClaimsJws(jwtDto.getAccessToken()).getBody();
//            log.info(claims.toString());

            if(claims.getExpiration().before(new Date())){
                log.info("기한이 만료됨 ");
                log.info("현재 시간 : {}",new Date());
                log.info("만료기간 : {}",claims.getExpiration());
                return false;
            }
            return true;
        }catch (ExpiredJwtException ex){
            log.info("만료된 토큰");
            log.info("현재 시간 : {}",new Date());
            log.info("만료기간 : {}",ex.getClaims().getExpiration());
            return false;
        }catch(UnsupportedJwtException ex){
            log.info("발급한 jwt accessToken아님 {}",jwtDto);
            return false;
        }catch(SignatureException ex){
            log.info("signature 일치하지않음");
            return false;
        }catch(IllegalArgumentException ex){
            log.info("jwt accessToken is null");
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
    //토큰 검증 - 필터에서 사용해야됨 -----------------------------------------------------
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
    public Claims getClaim(JwtDto jwtDto){
        try{
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtDto.getAccessToken())
                    .getBody();
        }catch(Exception ex){
            log.info("getClaim err");
            return null;
        }
    }
    public Long getExpiration(JwtDto jwtDto){
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(jwtDto.getAccessToken())
                    .getBody();
            Long now = new Date().getTime();
            return claims.getExpiration().getTime() - now;
        }
        catch(Exception ex){
            log.info("getExpiration err");
            return null;
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
    public Authentication authenticate(JwtDto jwtDto) throws AuthenticationException{
        Long id = getId(jwtDto);
        log.info("Member id = {}",id);
        Optional<Member> tmp = memberRepository.findByid(id);
        if(tmp.isEmpty()){
            log.info("It empty");
            return null;
        }
        MemberDto memberDto = tmp.get().toMemberDto();

        Claims claims = getClaim(jwtDto);
        List<? extends GrantedAuthority> authorities = Arrays.stream(
                claims.get("roles").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        UserPrinciple principle = new UserPrinciple(String.valueOf(id), memberDto.getNickname(),authorities);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =  new UsernamePasswordAuthenticationToken(principle,jwtDto.getAccessToken(),authorities);
//        return authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        return usernamePasswordAuthenticationToken;
    }
}
