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
    private Set<String> preferences;
    @Enumerated(EnumType.STRING)
    private Role role;
    public Member(Long id, String email, String nickname){
        this.id=id;
        this.email = email;
        this.nickname = nickname;
        this.preferences = new HashSet<>();
    }
}
