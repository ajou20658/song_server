package com.example.cleancode.config;

//import com.example.cleancode.filter.JwtTokenFilter;
//import com.example.cleancode.filter.JwtTokenFilter;
import com.example.cleancode.filter.LogFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LogConfig {
//    @Autowired
//    private JwtTokenProvider jwtTokenProvider;
//    @Autowired
//    private MemberRepository memberRepository;
//    @Autowired
//    private LoginService loginService;
    @Bean
    public FilterRegistrationBean logFilter(){
        FilterRegistrationBean<Filter> filterFilterRegistrationBean =
                new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new LogFilter());
        filterFilterRegistrationBean.setOrder(1);
        filterFilterRegistrationBean.addUrlPatterns("/*");
        return filterFilterRegistrationBean;
    }
    @Bean
    public CharacterEncodingFilter characterEncodingFilter(){
        CharacterEncodingFilter characterEncodingFilter =  new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }
//    @Bean
//    public FilterRegistrationBean jwtFilter(){
//        FilterRegistrationBean<Filter> filterFilterRegistrationBean =
//                new FilterRegistrationBean<>();
//        filterFilterRegistrationBean.setFilter(new JwtTokenFilter(jwtTokenProvider,memberRepository,loginService));
//        filterFilterRegistrationBean.setOrder(2);
//        filterFilterRegistrationBean.addUrlPatterns("/*");
//        return filterFilterRegistrationBean;
//    }
    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }
}
