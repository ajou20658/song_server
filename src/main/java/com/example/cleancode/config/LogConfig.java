package com.example.cleancode.config;

import com.example.cleancode.filter.LogFilter;
import com.example.cleancode.utils.jwt.JwtService;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LogConfig {
//    @Bean
//    public FilterRegistrationBean logFilter(){
//        FilterRegistrationBean<Filter> filterFilterRegistrationBean =
//                new FilterRegistrationBean<>();
//        filterFilterRegistrationBean.setFilter(new LogFilter());
//        filterFilterRegistrationBean.setOrder(0);
//        filterFilterRegistrationBean.addUrlPatterns("/*");
//        return filterFilterRegistrationBean;
//    }
////    @Bean
////    public FilterRegistrationBean jwtFilter(){
////        FilterRegistrationBean<Filter> filterFilterRegistrationBean =
////                new FilterRegistrationBean<>();
////        filterFilterRegistrationBean.setFilter(new JwtAuthorizationFilter(jwtService));
////        filterFilterRegistrationBean.setOrder(1);
////        filterFilterRegistrationBean.addUrlPatterns("/*");
////        return filterFilterRegistrationBean;
////    }
    @Bean
    public CharacterEncodingFilter characterEncodingFilter(){
        CharacterEncodingFilter characterEncodingFilter =  new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }
}
