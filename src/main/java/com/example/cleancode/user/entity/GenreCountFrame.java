package com.example.cleancode.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class GenreCountFrame {
    @Builder.Default
    @Column(name = "J-POP")
    private Integer a=0;
    @Builder.Default
    @Column(name = "POP")
    private Integer b=0;
    @Builder.Default
    @Column(name = "R&B/Soul")
    private Integer c=0;
    @Builder.Default
    @Column(name = "국내드라마")
    private Integer d=0;
    @Builder.Default
    @Column(name = "댄스")
    private Integer e=0;
    @Builder.Default
    @Column(name = "랩/힙합")
    private Integer f=0;
    @Builder.Default
    @Column(name = "록/메탈")
    private Integer g=0;
    @Builder.Default
    @Column(name = "발라드")
    private Integer h=0;
    @Builder.Default
    @Column(name = "성인가요/트로트")
    private Integer i=0;
    @Builder.Default
    @Column(name = "애니메이션/웹툰")
    private Integer j=0;
    @Builder.Default
    @Column(name = "인디음악")
    private Integer k=0;
    @Builder.Default
    @Column(name = "일렉트로니카")
    private Integer l=0;
    @Builder.Default
    @Column(name = "포크/블루스")
    private Integer m=0;
}
