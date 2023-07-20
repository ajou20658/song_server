package com.example.cleancode.login.JpaRepository;

import com.example.cleancode.login.entity.KakaoToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<KakaoToken,Long> {
    Optional<KakaoToken> findById(Long id);
}
