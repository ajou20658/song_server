package com.example.cleancode.utils;

import java.net.URI;

public enum URIS {
    LOGIN("/login");
    private final String uri;
    URIS(String uri){
        this.uri = uri;
    }

    public String getUri(){
        return uri;
    }
}
