package com.example.cleancode.song.repository;

import com.example.cleancode.song.entity.Chart100;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChartRepository extends JpaRepository<Chart100,Long> {
    Optional<Chart100> findById(Long id);
    Page<Chart100> findByArtistOrderByIdDesc(String artist, Pageable pageable);
    Page<Chart100> findByArtistContaining(String artist,Pageable pageable);
    Optional<Chart100> findByArtistAndTitle(String artist,String title);
}