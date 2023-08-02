package com.example.cleancode.song.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Album {
    @Id
    private Long albumId;
    private String albumName;
    @OneToMany(mappedBy = "album",cascade = CascadeType.ALL)
    private List<Song> songs;
}
