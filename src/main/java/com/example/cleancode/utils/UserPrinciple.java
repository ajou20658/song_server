package com.example.cleancode.utils;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserPrinciple extends User {
    private Long Id;
    public UserPrinciple(Long Id,String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.Id=Id;

    }

    @Override
    public String toString() {
        return "UserPrinciple(Id = "+Id+" username = "+getUsername()
                +"role = "+getAuthorities();
    }
}
