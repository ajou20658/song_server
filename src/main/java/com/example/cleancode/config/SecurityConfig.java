package com.example.cleancode.config;

//import com.example.cleancode.filter.JwtAuthorizationFilter;
//import com.example.cleancode.user.service.oauth2.CustomOAuth2UserService;
import com.example.cleancode.filter.JwtAuthorizationFilter;
import com.example.cleancode.utils.URIS;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthorizationFilter jwtAuthorizationFilter;
    String[] whitelist = {"/song/**","/login","/member/**","/"};
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/member/**").hasRole("USER")
                        .anyRequest().permitAll()
        );
        http.sessionManagement(session -> session
                .disable());
        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer
                .disable());
        return http.build();
    }

//    private AuthenticationSuccessHandler successHandler() {
//        return(((request, response, authentication) -> {
//            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
//            String id = defaultOAuth2User.getAttributes().get("id").toString();
//            String body= """
//                    {"id":"%s"}
//                    """.formatted(id);
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
//
//            PrintWriter writer = response.getWriter();
//            writer.println(body);
//            writer.flush();
//        }));
//    }
}
