package com.example.cleancode.ddsp.entity;

import com.example.cleancode.ddsp.entity.etc.InferenceRedisEntity;
import com.example.cleancode.song.entity.ProgressStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
@RequiredArgsConstructor
public class InferenceQueue {
    private final RedisTemplate<String, ProgressStatus> redisTemplate;

    public String getKey(InferenceRedisEntity inferenceRedisEntity) {
        return inferenceRedisEntity.getSongId() + ":" + inferenceRedisEntity.getPtrId();
    }

    public void pushInProgress(InferenceRedisEntity inferenceRedisEntity) {
        String key = getKey(inferenceRedisEntity);
        redisTemplate.opsForValue().set(key, ProgressStatus.PROGRESS);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }

    public void changeStatus(InferenceRedisEntity inferenceRedisEntity, ProgressStatus progressStatus) {
        String key = getKey(inferenceRedisEntity);
        redisTemplate.opsForValue().set(key, progressStatus);
    }

    public Set<String> getAllKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public ProgressStatus getData(InferenceRedisEntity inferenceRedisEntity) {
        String key = getKey(inferenceRedisEntity);
        ProgressStatus status = redisTemplate.opsForValue().get(key);
        if(status==null){
            log.error("No data found for inference key : {}",key);
        }
        return status;
    }
}
