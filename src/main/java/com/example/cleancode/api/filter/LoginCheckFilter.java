package com.example.cleancode.api.filter;

import com.example.cleancode.api.entity.OAuthTokenRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class LoginCheckFilter implements Filter {

    public static final String[] whitelist = {"/api","/api/login"};
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

//        HttpServletRequest httpResponse = (HttpServletRequest) response;

        try{
            log.info("인증 체크 필터 시작 {}",requestURI);
            if(isLoginCheckPath(requestURI)){
                log.info("인증 체크 로직 실행 {}",requestURI);
                Cookie[] cookies = httpRequest.getCookies();
                Arrays.stream(cookies).iterator().forEachRemaining(
                        cookie -> {
                            log.info("Cookies"+cookie);
                        }
                );
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



