package com.example.cleancode.song.repository;

import com.example.cleancode.song.dto.SongFormat;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song,Long> {
    Optional<Song> findById(Long id);
    List<Song> findByArtistContaining(String artist);
    List<Song> findByTitleContaining(String title);
    List<Song> findByArtistContainingOrTitleContaining(String artist,String title);
    List<Song> findByIsTop(boolean isTop);
    @Query("SELECT s FROM Song s WHERE s.originUrl <> ''")
    List<Song> findByOriginUrlIsNotNull();
    List<Song> findByStatus(ProgressStatus progressStatus);
    List<Song> findByArtistAndTitle(String artist,String title);
    List<Song> findByStatusIsNull();
    Optional<Song> findByOriginUrl(String uuid);
}