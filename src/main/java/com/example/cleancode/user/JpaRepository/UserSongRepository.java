package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSongRepository extends JpaRepository<UserSong,Long> {
    List<UserSong> findByMemberId(Long memberId);
}
