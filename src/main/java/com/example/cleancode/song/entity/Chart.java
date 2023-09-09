package com.example.cleancode.song.entity;

import com.example.cleancode.song.dto.ChartDTO;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@Builder
public class Chart {
    @Id
    public String songId;
    public String title;
    public String imgUrl;
    public String artist;
    public String albumId;

    @ElementCollection
    public List<String> genre;

    public ChartDTO toChartDto(){
        return ChartDTO.builder()
                .songId(songId)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .albumId(albumId)
                .genre(genre)
                .build();
    }
}
