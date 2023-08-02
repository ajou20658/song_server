package com.example.cleancode.song.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChartDTO {
    private String songId;
    private String title;
    private String imgUrl;
    private String artist;
    private String albumId;
    private String genre;
}
