package com.example.cleancode.login.filter;

import com.example.cleancode.login.service.jwt.JwtTokenProvider;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LoginFilter implements Filter {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.token.expiration-time}")
    private Long tokenMillisecond;
    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshMillisecond;
    public static final String[] whitelist = {"/v2/login","/v2/test","/admin/members"};
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try{
            log.info("인증 체크 필터 시작 {}",requestURI);
            if (isLoginCheckPath(requestURI)) {
                log.info("인증 체크 로직 실행 {}", requestURI);
                Cookie[] cookies = httpRequest.getCookies();
                if (cookies != null) {
                    Map<String, String> cookieMap = new HashMap<>();
                    List<Cookie> cookieList = Arrays.asList(cookies);
                    for (Cookie cookie : cookieList) {
                        cookieMap.put(cookie.getName(), cookie.getValue());
                    }
                    // Now you have the cookies stored in the 'cookieMap'
                    boolean isValidate = jwtTokenProvider.validateToken(cookieMap.get("jwtCookie"));
                    //토큰이 만료되었으면
                    if(!isValidate){
                        boolean isRefreshValidate = jwtTokenProvider.validateToken(cookieMap.get("jwtRefresh"));
                        //리프레시토큰도 만료되었으면
                        if(!isRefreshValidate){
                            log.info("올바르지 않은 jwt");
                            //프론트 위치로 리다이렉트 시키기
                            return;
                        }
                        //리프레시토큰은 유효할경우 새로 발급
                        String refreshToken = cookieMap.get("jwtRefresh");
                        Long id = jwtTokenProvider.getClaim(refreshToken);
                        String jwtToken = jwtTokenProvider.generateToken(id);
                        String jwtRefresh = jwtTokenProvider.generateRefreshToken(id);
                        Cookie jwtCookie = new Cookie("jwtCookie",jwtToken);
                        jwtCookie.setPath("/");
                        jwtCookie.setMaxAge(Math.toIntExact(tokenMillisecond));
                        httpResponse.addCookie(jwtCookie);

                        Cookie Refresh = new Cookie("jwtRefresh",jwtRefresh);
                        Refresh.setPath("/");
                        Refresh.setMaxAge(Math.toIntExact(refreshMillisecond));
                        httpResponse.addCookie(Refresh);
                    }
                    log.info("Stored Cookies: {}", cookieMap);
                    // Perform any necessary checks on the cookie values here
                } else {
                    log.info("No cookies found.");
                    // 프론트 로그인 위치로 리다이렉트 필요
                    return;
                }
            }
            chain.doFilter(request,response);
        }catch(Exception e){
            throw e;
        }finally {
            log.info("인증 체크 필터 종료 {}",requestURI);
        }
    }

    private boolean isLoginCheckPath(String requestURI){

        return !PatternMatchUtils.simpleMatch(whitelist,requestURI);
    }
}
