package com.example.cleancode.song.dto;

import lombok.Data;

@Data
public class SearchDto {
    private String title;
    private String artist;
    private String albumName;
    private Long songId;
    private Long albumId;
}
