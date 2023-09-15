package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.FilePath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilePathRepository extends JpaRepository<FilePath,Long> {
}
