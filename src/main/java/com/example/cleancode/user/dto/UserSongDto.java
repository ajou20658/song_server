package com.example.cleancode.user.dto;

import com.example.cleancode.user.entity.UserSong;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSongDto {
    private Long id;
    private String awsUrl;
    private List<Float> spectr;
    private LocalDateTime createdAt;


    public UserSong UserSongDto(){
        return UserSong.builder()
                .id(id)
                .awsUrl(awsUrl)
                .spectr(spectr)
                .createdAt(createdAt)
                .build();
    }
}
