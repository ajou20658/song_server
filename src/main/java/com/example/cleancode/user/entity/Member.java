package com.example.cleancode.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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
    @Column(name = "preference_Genre", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private Set<String> preference_Genre;
    @Column(name = "preference_Singer", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private Set<String> preference_Singer;
    @Column(name = "preference_Title", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private Set<String> preference_Title;
    @Enumerated(EnumType.STRING)
    private Role role;
    public Member(Long id, String email, String nickname){
        this.id=id;
        this.email = email;
        this.nickname = nickname;
        this.preference_Genre = new HashSet<>();
        this.preference_Singer = new HashSet<>();
        this.preference_Title = new HashSet<>();
    }
}
