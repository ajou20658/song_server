package com.example.cleancode.ddsp.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResultSongDto {
    private Integer id;
    private String generatedUrl;
    private String vocalName;
    private String songName;
}
