package com.example.cleancode.user.entity;

import com.example.cleancode.user.dto.FilePathDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
public class FilePath {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userId;
    private String uri;
    public FilePathDto toFilePathDto(){
        return FilePathDto.builder()
                .userId(userId)
                .uri(uri)
                .build();
    }
}
