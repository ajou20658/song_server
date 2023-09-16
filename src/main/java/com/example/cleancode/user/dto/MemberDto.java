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
    private String profileUrl;
    private Role role;
    public Member makeMember(){
        return Member.builder()
                .role(role)
                .email(email)
                .id(id)
                .nickname(nickname)
                .profileUrl(profileUrl)
                .build();
    }
}
