package com.example.cleancode.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class GenreCountFrame {
    @Column(name = "J-POP")
    private Integer a;
    @Column(name = "POP")
    private Integer b;
    @Column(name = "R&B/Soul")
    private Integer c;
    @Column(name = "국내드라마")
    private Integer d;
    @Column(name = "댄스")
    private Integer e;
    @Column(name = "랩/힙합")
    private Integer f;
    @Column(name = "록/메탈")
    private Integer g;
    @Column(name = "발라드")
    private Integer h;
    @Column(name = "성인가요/트로트")
    private Integer i;
    @Column(name = "애니메이션/웹툰")
    private Integer j;
    @Column(name = "인디음악")
    private Integer k;
    @Column(name = "일렉트로니카")
    private Integer l;
    @Column(name = "포크/블루스")
    private Integer m;
}
