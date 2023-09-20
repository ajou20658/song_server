package com.example.cleancode.user.entity;

import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.utils.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    private Long id;
    private String email;
    @Column(name = "nickname", nullable = false, columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String nickname;
    @Column(name = "profileUrl")
    private String profileUrl;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ElementCollection
    private List<Long> selected;

    public UserDto toMemberDto(){
        return UserDto.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileUrl(profileUrl)
                .role(role)
                .selected(selected)
                .build();
    }
}
