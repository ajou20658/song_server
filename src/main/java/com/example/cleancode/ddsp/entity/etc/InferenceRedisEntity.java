package com.example.cleancode.ddsp.entity.etc;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InferenceRedisEntity {
    private String songId;
    private String ptrId;
}
