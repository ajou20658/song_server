package com.example.cleancode.user.entity;

import com.example.cleancode.song.entity.Song;
import com.example.cleancode.user.dto.UserSongDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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
    @Column(name = "awsUrl", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String awsUrl;
    @ElementCollection
    private List<Float> spectr;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    public UserSongDto toUserSongDto(){
        return UserSongDto.builder()
                .id(id)
                .song(song)
                .awsUrl(awsUrl)
                .spectr(spectr)
                .createdAt(createdAt)
                .user(user)
                .build();
    }
}
