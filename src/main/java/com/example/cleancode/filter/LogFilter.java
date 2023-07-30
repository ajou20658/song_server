package com.example.cleancode.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Log Filter Init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String uuid = UUID.randomUUID().toString();
        String content = httpRequest.getQueryString();
        try{
            log.info("REQUEST [{}][{}][{}]",uuid,requestURI,content);
            chain.doFilter(request,response);
        }catch (Exception e){
            throw e;
        }finally {
            log.info("RESPONSE [{}][{}]",uuid,requestURI);
        }
    }

    @Override
    public void destroy() {
        log.info("Log Filter Destroy");
    }
}
