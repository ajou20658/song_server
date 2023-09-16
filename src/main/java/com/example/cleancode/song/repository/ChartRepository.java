package com.example.cleancode.song.repository;

import com.example.cleancode.song.entity.Chart100;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChartRepository extends JpaRepository<Chart100,Long> {
    Optional<Chart100> findById(Long id);
}