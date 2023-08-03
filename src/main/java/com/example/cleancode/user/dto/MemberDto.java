package com.example.cleancode.user.dto;

import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import lombok.*;

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
    private Set<String> preferences;
    private Role role;
    public Member makeMember(){
        return Member.builder()
                .role(role)
                .email(email)
                .id(id)
                .nickname(nickname)
                .preferences(preferences)
                .build();
    }
}
