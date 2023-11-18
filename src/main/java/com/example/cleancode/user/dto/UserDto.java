package com.example.cleancode.user.dto;

import com.example.cleancode.user.entity.User;
import com.example.cleancode.utils.Role;
import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileUrl;
    private Role role;
    private List<Long> selected;
    private List<Long> recommand;
    public User makeMember(){
        return User.builder()
                .role(role)
                .email(email)
                .id(id)
                .nickname(nickname)
                .profileUrl(profileUrl)
                .selected(selected)
                .recommand(recommand)
                .build();
    }
}
