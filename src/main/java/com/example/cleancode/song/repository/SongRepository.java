package com.example.cleancode.song.repository;

import com.example.cleancode.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song,Long> {
    Optional<Song> findById(Long id);
    Page<Song> findByArtistOrderByIdDesc(String artist, Pageable pageable);
    Page<Song> findByArtistContaining(String artist, Pageable pageable);
    Optional<Song> findByArtistAndTitle(String artist, String title);
    List<Song> findByArtistContaining(String artist);
    List<Song> findByTitleContaining(String title);
}