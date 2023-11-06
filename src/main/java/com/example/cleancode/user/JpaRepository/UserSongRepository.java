package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSongRepository extends JpaRepository<UserSong,Long> {
    List<UserSong> findByUserId(Long userId);
    @Query("SELECT us FROM UserSong us WHERE us.song.id = :songId AND us.user.id = :userId")
    Optional<UserSong> findBySongIdAndUserId(@Param("songId") Long songId, @Param("userId") Long userId);
    Optional<UserSong> findByVocalUrl(String vocalUrl);
    Optional<UserSong> findByOriginUrl(String originUrl);
}
