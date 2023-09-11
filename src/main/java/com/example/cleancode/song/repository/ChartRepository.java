package com.example.cleancode.song.repository;

import com.example.cleancode.song.entity.Chart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChartRepository extends JpaRepository<Chart,String> {
    Optional<Chart> findById(String id);
    List<Chart> findByArtist(String artist);
    Optional<Chart> findByTitle(String title);
}
