package com.example.cleancode.song.dto;

import com.example.cleancode.song.entity.Chart;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChartDTO {
    private String songId;
    private String title;
    private String imgUrl;
    private String artist;
    private String albumId;
    private List<String> genre;

    public Chart toChartEntity(){
        return Chart.builder()
                .songId(songId)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .albumId(albumId)
                .genre(genre)
                .build();
    }
}
