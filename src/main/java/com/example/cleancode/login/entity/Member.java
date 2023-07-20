package com.example.cleancode.login.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    private Long id;
    private String email;
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
