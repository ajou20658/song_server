package com.example.cleancode.user.entity;

import com.example.cleancode.user.dto.MemberDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    private Long id;

    private String email;
    @Column(name = "nickname", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String nickname;
    @Column(name = "profile")
    private String profile;
    @Column(name = "preference_Genre", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private Set<String> preference_Genre;
    @Column(name = "preference_Singer", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private Set<String> preference_Singer;
    @Column(name = "preference_Title", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private Set<String> preference_Title;
    @Enumerated(EnumType.STRING)
    private Role role;

    public MemberDto toMemberDto(){
        return MemberDto.builder()
                .preference_Genre(preference_Genre)
                .id(id)
                .email(email)
                .nickname(nickname)
                .profile(profile)
                .preference_Singer(preference_Singer)
                .preference_Title(preference_Title)
                .role(role)
                .build();
    }
}
