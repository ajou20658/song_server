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
            @JsonProperty("f0_1") String f0_1,
            @JsonProperty("f0_2") String f0_2,
            @JsonProperty("f0_3") String f0_3,
            @JsonProperty("f0_4") String f0_4,
            @JsonProperty("f0_5") String f0_5,
            @JsonProperty("f0_6") String f0_6,
            @JsonProperty("f0_7") String f0_7,
            @JsonProperty("f0_8") String f0_8) {
        this.f0_1 = Integer.valueOf(f0_1);
        this.f0_2 = Integer.valueOf(f0_2);
        this.f0_3 = Integer.valueOf(f0_3);
        this.f0_4 = Integer.valueOf(f0_4);
        this.f0_5 = Integer.valueOf(f0_5);
        this.f0_6 = Integer.valueOf(f0_6);
        this.f0_7 = Integer.valueOf(f0_7);
        this.f0_8 = Integer.valueOf(f0_8);
    }
}
