package com.example.cleancode.ddsp.entity;

import com.example.cleancode.song.entity.Song;
import com.example.cleancode.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultSong {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "generatedUrl", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String generatedUrl;
    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;
    @ManyToOne
    @JoinColumn(name = "ptrData_id")
    private PtrData ptrData;
}
