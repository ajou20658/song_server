package com.example.cleancode.filter;

import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.utils.jwt.JwtService;
import com.example.cleancode.utils.jwt.TokenStatus;
import com.example.cleancode.utils.jwt.TokenValidationResult;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AUTH;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken)&&bearerToken.startsWith("Bearer")){
            return bearerToken.substring(7);
        }
        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        log.info("accessToken {}",token);
        String refresh = request.getParameter("refresh");
        JwtDto jwtDto = new JwtDto(token,"");
        Authentication auth;
        if(StringUtils.hasText(refresh)){
            jwtDto.setRefreshToken(refresh);
        }
        if(StringUtils.hasText(token)){
            log.info(jwtDto.getAccessToken());
            if(jwtService.validateToken(jwtDto)){
                try {
                    auth = token != null ? jwtService.authenticate(jwtDto) : null;
                } catch (AuthenticationException e) {
                    throw new RuntimeException(e);
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("AUTH SUCCESS : {}",auth.getName());
            }
//            }else{
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                response.getWriter().write("권한이 없습니다");
//                response.getWriter().flush();
//                response.getWriter().close();
//                return;
//            }
        }else {
            log.info("No Authorization Header");
            request.setAttribute("result",new TokenValidationResult(false,null,null, TokenStatus.NO_AUTH_HEADER,null));
        }
        filterChain.doFilter(request,response);
    }
}
