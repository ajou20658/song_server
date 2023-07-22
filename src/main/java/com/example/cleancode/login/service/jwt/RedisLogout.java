package com.example.cleancode.login.service.jwt;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisLogout {
    private final RedisTemplate<String ,Object> redisTemplate;

    public RedisLogout(RedisTemplate<String,Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void setValue(String key, Object value){
        redisTemplate.opsForValue().set(key,value);
    }

    public Object getValue(String key){
        return redisTemplate.opsForValue().get(key);
    }
}
