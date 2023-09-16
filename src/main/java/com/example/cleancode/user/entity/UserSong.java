package com.example.cleancode.user.entity;

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
    @Column(name = "awsUrl", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String awsUrl;
    @ElementCollection
    private List<Float> spectr;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private User member;
    public UserSongDto UserSong(){
        return UserSongDto.builder()
                .id(id)
                .awsUrl(awsUrl)
                .spectr(spectr)
                .createdAt(createdAt)
                .build();
    }
}
