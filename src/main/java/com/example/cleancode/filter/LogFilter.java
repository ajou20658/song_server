package com.example.cleancode.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LogFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String uuid = UUID.randomUUID().toString();
        String content = httpRequest.getQueryString();
        try{
            log.info("REQUEST [{}][{}][{}]",uuid,requestURI,content);
            chain.doFilter(request,response);
        }finally {
            log.info("RESPONSE [{}][{}]", uuid, requestURI);
        }
    }
}
