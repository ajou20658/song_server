package com.example.cleancode.image.repository;

import com.example.cleancode.image.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album,Long> {
}
