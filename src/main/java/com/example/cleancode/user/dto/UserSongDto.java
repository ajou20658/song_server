package com.example.cleancode.user.dto;

import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
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
    private static SongRepository songRepository;
    private Long id;
    private Long songId;
    private String awsUrl;
    private String originUrl;
    private List<Integer> spectr;
    private LocalDateTime createdAt;
    private User user;
    private ProgressStatus status;

    public UserSong toUserSong(){
        Song song = songRepository.findById(songId).get();
        return UserSong.builder()
                .id(id)
                .song(song)
                .awsUrl(awsUrl)
                .originUrl(originUrl)
                .spectr(spectr)
                .status(status)
                .user(user)
                .build();
    }
}
