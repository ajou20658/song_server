package com.example.cleancode.song.dto;

import com.example.cleancode.user.entity.Dataframe2Json;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SongOutput {
    private Integer id;
    private String title;
    private String artist;
    private Integer like;
    private String genre;
    private String encodedGenre;
    private Integer f0_1;
    private Integer f0_2;
    private Integer f0_3;
    private Integer f0_4;
    private Integer f0_5;
    private Integer f0_6;
    private Integer f0_7;
    private Integer f0_8;
}
