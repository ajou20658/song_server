package com.example.cleancode.image.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Song {
    @Id
    private Long songId;
    private String uri;
    private String songTitle;
    private String artist;
    private String genre;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;
}
