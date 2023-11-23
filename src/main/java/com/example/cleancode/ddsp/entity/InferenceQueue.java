package com.example.cleancode.ddsp.entity;

import com.example.cleancode.song.entity.ProgressStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InferenceQueue {
    private final String TASKS_KEY_PREFIX = "inference:";
    private RedisTemplate<String,ProgressStatus> redisTemplate;
    private SetOperations<String, ProgressStatus> setOperations;
    private Integer MAX_COUNT=8;
    public void pushIn(Long songId){
        String redisKey = TASKS_KEY_PREFIX + songId;
        redisTemplate.opsForValue().set(redisKey,ProgressStatus.PROGRESS);
        redisTemplate.expire(redisKey,5, TimeUnit.MINUTES);
    }
    public void changeStatus(Long songId){
        String redisKey = TASKS_KEY_PREFIX + songId;
        redisTemplate.opsForValue().set(redisKey,ProgressStatus.COMPLETE);
    }
    public long getCountOfProgressStatus() {
        Set<String> keys = redisTemplate.keys(TASKS_KEY_PREFIX + "*");
        long count = 0;
        for (String key : keys) {
            ProgressStatus value = redisTemplate.opsForValue().get(key);
            if (value != null && value.equals(ProgressStatus.PROGRESS)) {
                count++;
            }
        }
        return count;
    }
}
