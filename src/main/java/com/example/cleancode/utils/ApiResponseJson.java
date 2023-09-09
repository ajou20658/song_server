package com.example.cleancode.utils;

import org.springframework.http.HttpStatus;

public class ApiResponseJson {
    public HttpStatus httpStatus;
    public int code;
    public Object data;
    public ApiResponseJson(HttpStatus httpStatus,int code, Object data){
        this.httpStatus = httpStatus;
        this.code = code;
        this.data = data;
    }
    public ApiResponseJson(HttpStatus httpStatus,Object data){
        this.httpStatus = httpStatus;
        this.data = data;
    }
}
