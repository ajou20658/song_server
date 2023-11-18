package com.example.cleancode.ddsp.repository;

import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultSongRepository extends JpaRepository<ResultSong,Integer> {
    List<ResultSong> findResultSongsByPtrData(PtrData ptrData);
}
