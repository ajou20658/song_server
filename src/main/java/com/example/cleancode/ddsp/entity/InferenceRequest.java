package com.example.cleancode.ddsp.entity;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public class InferenceRequest {
    private Long targetVoiceId;
    private Long targetSongId;
}
