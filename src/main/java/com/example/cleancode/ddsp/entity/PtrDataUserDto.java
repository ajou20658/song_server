package com.example.cleancode.ddsp.entity;

import lombok.*;

@Getter
@Builder
public class PtrDataUserDto {
    private Long id;
    private String name;
    private String ptrUrl;
    public PtrData ptrData(){
        return PtrData.builder()
                .id(id)
                .name(name)
                .ptrUrl(ptrUrl)
                .build();
    }
}
