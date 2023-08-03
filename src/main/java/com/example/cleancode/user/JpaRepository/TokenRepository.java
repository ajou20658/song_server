package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.KakaoToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<KakaoToken,Long> {
    Optional<KakaoToken> findById(Long id);
}
