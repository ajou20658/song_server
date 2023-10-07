package com.example.cleancode.aws.entity;

import lombok.Builder;
import lombok.Data;

@Data
public class UploadStatus {
    private String status;
    private Long expectTime;
}
