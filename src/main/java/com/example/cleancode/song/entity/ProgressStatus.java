package com.example.cleancode.song.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
@Getter
@RequiredArgsConstructor
public enum ProgressStatus {
    PROGRESS("progress"),
    ERROR("stop"),
    COMPLETE("complete");

    private final String message;
}
