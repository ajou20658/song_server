package com.example.cleancode.song.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
@Getter
@RequiredArgsConstructor
@ToString
public enum ProgressStatus {
    NONE("NONE"),
    UPLOADED("UPLOADED"),
    PROGRESS("PROGRESS"),
    ERROR("ERROR"),
    COMPLETE("COMPLETE");

    private final String message;
}
