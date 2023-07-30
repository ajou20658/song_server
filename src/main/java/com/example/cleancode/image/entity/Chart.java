package com.example.cleancode.image.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
public class Chart {
    @Id
    public String songId;
    public String title;
    public String imgUrl;
    public String artist;
    public String albumId;
    public String genre;
}
