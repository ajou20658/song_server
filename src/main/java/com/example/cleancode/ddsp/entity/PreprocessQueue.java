package com.example.cleancode.ddsp.entity;

import com.example.cleancode.ddsp.entity.etc.InferenceRedisEntity;
import com.example.cleancode.ddsp.entity.etc.PreProcessRedisEntity;
import com.example.cleancode.song.entity.ProgressStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor
public class PreprocessQueue {
    @Qualifier("redisTemplate2")
    private RedisTemplate<String, ProgressStatus> redisTemplate;
    private String getKey(PreProcessRedisEntity preProcessRedisEntity) {
        // 여기에서 원하는 방식으로 키를 생성합니다.
        return preProcessRedisEntity.getUuid() + ":" + preProcessRedisEntity.getSongId() + ":" + preProcessRedisEntity.getUserId();
    }
    public void pushInProgress(PreProcessRedisEntity preProcessRedisEntity) {
        String key = getKey(preProcessRedisEntity);
        redisTemplate.opsForValue().set(key, ProgressStatus.PROGRESS);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }

    public void changeStatus(PreProcessRedisEntity preProcessRedisEntity, ProgressStatus progressStatus) {
        String key = getKey(preProcessRedisEntity);
        redisTemplate.opsForValue().set(key, progressStatus);
    }
    public boolean isExist(PreProcessRedisEntity preProcessRedisEntity){
        String key = getKey(preProcessRedisEntity);
        Set<String> allKey = getAllKeys("*");
        return allKey.contains(key);
    }
    public Set<String> getAllKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public ProgressStatus getData(PreProcessRedisEntity preProcessRedisEntity) {
        String key = getKey(preProcessRedisEntity);
        return redisTemplate.opsForValue().get(key);
    }

}
