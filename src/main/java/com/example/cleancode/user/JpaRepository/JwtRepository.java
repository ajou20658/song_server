package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.Jwt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtRepository extends JpaRepository<Jwt,Long> {
}
