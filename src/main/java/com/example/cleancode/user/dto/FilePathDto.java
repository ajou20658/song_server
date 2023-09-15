package com.example.cleancode.user.dto;

import com.example.cleancode.user.entity.FilePath;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FilePathDto {
    private Long id;
    private String uri;
    private String userId;
    public FilePath filePath(){
        return FilePath.builder()
                .uri(uri)
                .userId(userId)
                .build();
    }
}
