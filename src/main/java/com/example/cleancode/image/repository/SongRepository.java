package com.example.cleancode.image.repository;

import com.example.cleancode.image.dto.MelonDTO;
import com.example.cleancode.image.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song,Long> {
}
