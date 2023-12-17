package com.example.cleancode.song.dto;

import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SongDto {
    private Long id;
    private String title;
    private String imgUrl;
    private String artist;
    private List<String> genre;
    private String instUrl;
    private String originUrl;
    private ProgressStatus status;
}
