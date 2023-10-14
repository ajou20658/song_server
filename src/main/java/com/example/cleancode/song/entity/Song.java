package com.example.cleancode.song.entity;

import com.example.cleancode.song.dto.SongDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Song {
    @Id
    private Long id;
    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;
    private String imgUrl;
    @Column(name = "artist", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String artist;
    private boolean isTop;
    @ElementCollection
    private List<Float> spectr;
    @Column(name = "vocalUrl", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String vocalUrl;
    @Column(name = "instrUrl", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String instUrl;
    @Column(name = "originUrl", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String originUrl;
    @Column(name = "genre", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    @ElementCollection
    public List<String> genre;
    @ElementCollection
    public List<Long> encoded_genre;
    public SongDto toSongDto(){
        return SongDto.builder()
                .id(id)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .genre(genre)
                .encoded_genre(encoded_genre)
                .isTop(isTop)
                .originUrl(originUrl)
                .spectr(spectr)
                .vocalUrl(vocalUrl)
                .instUrl(instUrl)
                .build();
    }
}
