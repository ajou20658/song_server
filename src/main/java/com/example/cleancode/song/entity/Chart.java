package com.example.cleancode.song.entity;

import com.example.cleancode.song.dto.ChartDTO;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.List;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chart {
    @Id
    public String songId;
    public String title;
    public String imgUrl;
    public String artist;
    public String albumId;
    public String likeId;

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
