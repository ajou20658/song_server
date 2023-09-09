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
    @Enumerated(EnumType.STRING)
    private Role role;

    public MemberDto toMemberDto(){
        return MemberDto.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profile(profile)
                .role(role)
                .build();
    }
}
