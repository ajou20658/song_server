package com.example.cleancode.ddsp.entity.etc;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PreProcessRedisEntity {
    private String uuid;
    private String songId;
    private String userId;
}
