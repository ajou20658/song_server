package com.example.cleancode.song.dto;

import com.example.cleancode.song.entity.Chart100;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChartDTO {
    private Long id;
    private String title;
    private String imgUrl;
    private String artist;
    private Long likeId;
    private List<String> genre;
    private List<Float> spectr;
    private boolean available;
    private String awsUrl;
    public Chart100 toChartEntity(){
        return Chart100.builder()
                .id(id)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .genre(genre)
                .spectr(spectr)
                .available(available)
                .awsUrl(awsUrl)
                .build();
    }
}
