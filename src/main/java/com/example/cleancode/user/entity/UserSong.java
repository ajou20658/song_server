package com.example.cleancode.user.entity;

import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.user.dto.UserSongDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    private Song song;
    @Column(name = "awsUrl", nullable = true, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String awsUrl;
    @Column(name = "originUrl", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String originUrl;
    @ElementCollection
    private List<Float> spectr;
    private ProgressStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    public UserSongDto toUserSongDto(){
        return UserSongDto.builder()
                .id(id)
                .songId(song.getId())
                .awsUrl(awsUrl)
                .originUrl(originUrl)
                .spectr(spectr)
                .status(status)
                .user(user)
                .build();
    }
    public  UserSong changeStatus(ProgressStatus progressStatus){
        return UserSong.builder()
                .id(id)
                .song(song)
                .awsUrl(awsUrl)
                .originUrl(originUrl)
                .spectr(spectr)
                .status(progressStatus)
                .user(user)
                .build();
    }
}
