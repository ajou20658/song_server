package com.example.cleancode.song.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class SongFormat {
    private Long id;
    private String title;
    private String artist;
    private List<Integer> spectr;
    private List<String> genre;
    private List<Long> encoded_genre;
}
