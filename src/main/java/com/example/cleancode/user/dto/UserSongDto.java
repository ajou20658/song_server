package com.example.cleancode.user.dto;

import com.example.cleancode.song.entity.Song;
import com.example.cleancode.user.entity.User;
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
    private Song song;
    private String awsUrl;
    private List<Float> spectr;
    private LocalDateTime createdAt;
    private User user;

    public UserSong toUserSong(){
        return UserSong.builder()
                .id(id)
                .song(song)
                .awsUrl(awsUrl)
                .spectr(spectr)
                .createdAt(createdAt)
                .user(user)
                .build();
    }
}
