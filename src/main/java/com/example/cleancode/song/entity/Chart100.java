package com.example.cleancode.song.entity;

import com.example.cleancode.song.dto.ChartDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chart100 {
    @Id
    private Long id;
    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;
    private String imgUrl;
    @Column(name = "artist", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String artist;
    @Column(name = "likeId", nullable = false)
    private Long likeId;
    @ElementCollection
    private List<Float> spectr;
    @Column(name = "awsUrl", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String awsUrl;
    private boolean available;

    @Column(name = "genre", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    @ElementCollection
    public List<String> genre;

    public ChartDTO toChartDto(){
        return ChartDTO.builder()
                .id(id)
                .title(title)
                .imgUrl(imgUrl)
                .artist(artist)
                .genre(genre)
                .likeId(likeId)
                .spectr(spectr)
                .available(available)
                .awsUrl(awsUrl)
                .build();
    }
}
