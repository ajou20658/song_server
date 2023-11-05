package com.example.cleancode.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
public class Dataframe2Json {
    private Integer f0_1;
    private Integer f0_2;
    private Integer f0_3;
    private Integer f0_4;
    private Integer f0_5;
    private Integer f0_6;
    private Integer f0_7;
    private Integer f0_8;
    @JsonCreator
    public Dataframe2Json(
            @JsonProperty("f0_1") Integer f0_1,
            @JsonProperty("f0_2") Integer f0_2,
            @JsonProperty("f0_3") Integer f0_3,
            @JsonProperty("f0_4") Integer f0_4,
            @JsonProperty("f0_5") Integer f0_5,
            @JsonProperty("f0_6") Integer f0_6,
            @JsonProperty("f0_7") Integer f0_7,
            @JsonProperty("f0_8") Integer f0_8) {
        this.f0_1 = f0_1;
        this.f0_2 = f0_2;
        this.f0_3 = f0_3;
        this.f0_4 = f0_4;
        this.f0_5 = f0_5;
        this.f0_6 = f0_6;
        this.f0_7 = f0_7;
        this.f0_8 = f0_8;
    }
}
