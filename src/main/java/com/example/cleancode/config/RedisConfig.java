package com.example.cleancode.config;

import com.example.cleancode.song.entity.ProgressStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

@Configuration
public class RedisConfig {
    @Bean
    public LettuceConnectionFactory connectionFactory(){
        return new LettuceConnectionFactory();
    }
    @Bean
    public RedisTemplate<String,ProgressStatus> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,ProgressStatus> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
    @Bean
    public SetOperations<String, ProgressStatus> setOperations(RedisTemplate<String,ProgressStatus> redisTemplate){
        return redisTemplate.opsForSet();
    }

}
