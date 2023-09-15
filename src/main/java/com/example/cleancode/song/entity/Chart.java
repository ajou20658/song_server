package com.example.cleancode.song.entity;

import com.example.cleancode.song.dto.ChartDTO;
import jakarta.persistence.Column;
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
    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    public String title;
    public String imgUrl;
    @Column(name = "artist", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    public String artist;
    public String albumId;
    public String likeId;
    public boolean available = false;

    @Column(name = "genre", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
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
                .available(available)
                .build();
    }
}
