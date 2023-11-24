package com.example.cleancode.ddsp.entity;

import com.example.cleancode.ddsp.entity.etc.PreProcessRedisEntity;
import com.example.cleancode.song.entity.ProgressStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PreprocessQueueTest {
    @Autowired
    private PreprocessQueue preprocessQueue;
    @Test
    public void testInferenceQueue(){
        PreProcessRedisEntity preProcessRedisEntity = PreProcessRedisEntity.builder()
                .songId("1234")
                .userId("9876")
                .uuid("asdbasdf")
                .build();
        preprocessQueue.pushInProgress(preProcessRedisEntity);

        ProgressStatus status = preprocessQueue.getData(preProcessRedisEntity);
        System.out.println("status:"+status);
    }
}