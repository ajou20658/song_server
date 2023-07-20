package com.example.cleancode.api.config;

import com.example.cleancode.api.filter.LogFilter;
import com.example.cleancode.api.filter.LoginCheckFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean logFilter(){
        FilterRegistrationBean<Filter> filterFilterRegistrationBean =
                new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new LogFilter());
        filterFilterRegistrationBean.setOrder(1);
        filterFilterRegistrationBean.addUrlPatterns("/*");
        return filterFilterRegistrationBean;
    }

//    @Bean
//    public FilterRegistrationBean loginCheckFilter(){
//        FilterRegistrationBean<Filter> filterFilterRegistrationBean =
//                new FilterRegistrationBean<>();
//        filterFilterRegistrationBean.setFilter(new LoginCheckFilter());
//        filterFilterRegistrationBean.setOrder(2);
//        filterFilterRegistrationBean.addUrlPatterns("/*");
//        return filterFilterRegistrationBean;
//    }

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

}
