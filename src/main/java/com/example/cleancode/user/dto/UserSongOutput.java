package com.example.cleancode.user.dto;

import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class UserSongOutput {
    private Long songId;
    private String awsUrl;
    private String originUrl;
    private List<Integer> spectr;
    private LocalDateTime createdAt;
    private Long userId;
    private ProgressStatus status;
}
