package com.example.cleancode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {
    @Value("${spring.django-url}")
    private String djangoUrl;
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://"+djangoUrl)
                .build();
    }
}
