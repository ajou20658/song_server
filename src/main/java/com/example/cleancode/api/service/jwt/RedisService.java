package com.example.cleancode.api.service.jwt;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisService {
    private final RedisTemplate<String ,Object> redisTemplate;

    public RedisService(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void setValue(String key, Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    public Object getValue(String key){
        return redisTemplate.opsForValue().get(key);
    }
}
