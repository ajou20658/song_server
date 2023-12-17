package com.example.cleancode.ddsp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PtrData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Column(name = "ptrUrl", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String ptrUrl;
    @ElementCollection
    private List<Integer> spectr;
    @ElementCollection
    private List<Long> selected;
    @ElementCollection
    private List<Long> recommandSongIds;
    public PtrDataUserDto ptrDataUserDto(){
        return PtrDataUserDto.builder()
                .id(id)
                .name(name)
                .ptrUrl(ptrUrl)
                .build();
    }
}
