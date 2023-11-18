package com.example.cleancode.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RecommandRequestDataFrame {
    private Spectr2DataFrame user_f0;
    private GenreCountFrame user_genre;

}
