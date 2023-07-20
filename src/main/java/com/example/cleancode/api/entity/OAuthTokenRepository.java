package com.example.cleancode.api.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken,String> {
    Optional<OAuthToken> findByEmail(String email);
}
