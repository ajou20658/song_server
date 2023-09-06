package com.example.cleancode.user.dto;

import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import jakarta.persistence.Column;
import lombok.*;

import java.util.List;
import java.util.Set;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MemberDto {
    private Long id;
    private String email;
    private String nickname;
    private String profile;
    private Set<String> preference_Genre;
    private Set<String> preference_Singer;
    private Set<String> preference_Title;
    private Role role;
    public Member makeMember(){
        return Member.builder()
                .role(role)
                .email(email)
                .id(id)
                .nickname(nickname)
                .profile(profile)
                .preference_Genre(preference_Genre)
                .preference_Title(preference_Title)
                .preference_Singer(preference_Singer)
                .build();
    }
}
