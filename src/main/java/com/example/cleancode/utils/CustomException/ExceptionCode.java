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
    USER_SONG_INVALID(BAD_REQUEST,"존재하지 않은 녹음"),
    WEB_CLIENT_ERROR(INTERNAL_SERVER_ERROR,"서버에러 발생"),
    WEB_SIZE_OVER(INTERNAL_SERVER_ERROR,"django서버 용량 초과"),
    FORMAT_ERROR(BAD_REQUEST,"upload format 에러"),
    SIZE_ERROR(BAD_REQUEST,"upload size 에러"),
    AWS_ERROR(BAD_REQUEST,"aws에 데이터 없음"),
    EXPIRED_JWT_ERROR(BAD_REQUEST,"만료된 JWT 토큰"),
    JWT_ERROR(BAD_REQUEST,"JWT 발급 실패"),
    PTR_ERROR(BAD_REQUEST,"존재하지 않는 ptr 파일"),
    RESULT_SONG_ERROR(BAD_REQUEST,"삭제되었거나 존재하지 않는 파일"),
    AWS_UPLOAD_ERROR(BAD_REQUEST,"업로드 실패")
    ;

    private final HttpStatus status;
    private final String message;
}
