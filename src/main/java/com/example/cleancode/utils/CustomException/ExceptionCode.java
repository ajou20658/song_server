package com.example.cleancode.utils.CustomException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.devtools.v85.layertree.model.StickyPositionConstraint;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
@RequiredArgsConstructor
@Getter
public enum ExceptionCode {
    USER_INVALID(BAD_REQUEST, "등록되지 않은 유저."),
    USER_AUTHORIZATION(FORBIDDEN,"로그인이 필요."),
    USER_AUTHENTICATION(UNAUTHORIZED,"인증되지 않은 사용자."),
    SONG_INVALID(BAD_REQUEST,"등록되지 않은 곡."),
    USER_SONG_INVALID(BAD_REQUEST,"존재하지 않은 곡");

    private final HttpStatus status;
    private final String message;
}
