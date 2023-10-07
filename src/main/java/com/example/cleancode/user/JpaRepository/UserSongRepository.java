package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSongRepository extends JpaRepository<UserSong,Long> {
    List<UserSong> findByUserId(Long userId);
    Optional<UserSong> findByUserIdAndSongId(Long userId,Long songId);
    Optional<UserSong> findByAwsUrl(String awsUrl);
}
