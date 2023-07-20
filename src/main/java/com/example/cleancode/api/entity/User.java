package com.example.cleancode.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Getter
@Setter
@Builder
public class User {
    @Id
    public String email;
    @Column
    public String name;
    @Column
    public Set<String> preferences = new HashSet<>();  //지금은 일단 셋으로 하고 이후 장르 구체화되면
    //key:value 로 one-hot 매핑하기
}
