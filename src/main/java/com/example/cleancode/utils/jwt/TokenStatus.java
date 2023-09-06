package com.example.cleancode.utils.jwt;

public enum TokenStatus {
    TOKEN_VALID,
    TOKEN_EXPIRED,
    TOKEN_IS_BLASKLIST,
    TOKEN_WRONG_SIGNATURE,
    TOKEN_ID_NOT_MATCH,
    TOKEN_VALIDATION_TRY_FAILED,
    NO_AUTH_HEADER
}
