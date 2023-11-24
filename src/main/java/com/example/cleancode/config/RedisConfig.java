package com.example.cleancode.config;

import com.example.cleancode.ddsp.entity.etc.PreProcessRedisEntity;
import com.example.cleancode.song.entity.ProgressStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

@Configuration
public class RedisConfig {
    @Bean //Inference queue용도
    public LettuceConnectionFactory connectionFactory1(){
        return new LettuceConnectionFactory();
    }
    @Bean(name = "redisTemplate1")
    public RedisTemplate<String, ProgressStatus> redisTemplate1(
            @Qualifier("connectionFactory1") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ProgressStatus> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean(name = "setOperations1")
    public SetOperations<String, ProgressStatus> setOperations1(
            @Qualifier("redisTemplate1") RedisTemplate<String, ProgressStatus> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    // 두 번째 Redis 저장소
    @Bean //preprocess queue용도
    public LettuceConnectionFactory connectionFactory2() {
        return new LettuceConnectionFactory();
    }

    @Bean(name = "redisTemplate2")
    public RedisTemplate<String, ProgressStatus> redisTemplate2(
            @Qualifier("connectionFactory2") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, ProgressStatus> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
    @Bean(name = "setOperations2")
    public SetOperations<String, ProgressStatus> setOperations2(
            @Qualifier("redisTemplate2") RedisTemplate<String, ProgressStatus> redisTemplate) {
        return redisTemplate.opsForSet();
    }
}
