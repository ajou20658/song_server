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
    private boolean isTop;
    private List<String> genre;
    private List<Long> encoded_genre;
    private List<Float> spectr;
    private String vocalUrl;
    private String instUrl;
    private String originUrl;
    public Song toSongEntity(){
        return Song.builder()
                .id(id)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .isTop(isTop)
                .genre(genre)
                .encoded_genre(encoded_genre)
                .spectr(spectr)
                .originUrl(originUrl)
                .vocalUrl(vocalUrl)
                .instUrl(instUrl)
                .build();
    }
}
