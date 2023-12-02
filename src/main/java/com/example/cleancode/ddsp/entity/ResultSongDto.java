package com.example.cleancode.ddsp.entity;

import com.example.cleancode.song.dto.SongDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResultSongDto {
    private Integer id;
    private String generatedUrl;
    private SongDto song;
}
