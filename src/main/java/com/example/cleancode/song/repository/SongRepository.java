package com.example.cleancode.song.repository;

import com.example.cleancode.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song,Long> {
    Optional<Song> findById(Long id);
    Optional<Song> findByArtistAndTitle(String artist, String title);
    List<Song> findByArtistContaining(String artist);
    List<Song> findByTitleContaining(String title);
    List<Song> findByArtistContainingOrTitleContaining(String artist,String title);
    List<Song> findByIsTop(boolean isTop);
    List<Song> findByOriginUrlIsNotNull();
}