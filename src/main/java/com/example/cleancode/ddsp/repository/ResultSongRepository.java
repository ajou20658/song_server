package com.example.cleancode.ddsp.repository;

import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import com.example.cleancode.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultSongRepository extends JpaRepository<ResultSong,Integer> {
    List<ResultSong> findResultSongsByPtrData(PtrData ptrData);
    Optional<ResultSong> findResultSongsByPtrData_IdAndSong_Id(Long ptrData_id, Long song_id);
}
