package com.example.cleancode.song.dto;

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
    private Long likeId;
    private List<String> genre;
    private List<Long> encoded_genre;
    private List<Float> spectr;
    private String vocalUrl;
    private String instUrl;

    public Song toChartEntity(){
        return Song.builder()
                .id(id)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .likeId(likeId)
                .genre(genre)
                .encoded_genre(encoded_genre)
                .spectr(spectr)
                .vocalUrl(vocalUrl)
                .instUrl(instUrl)
                .build();
    }
}
