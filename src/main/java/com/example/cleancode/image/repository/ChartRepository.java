package com.example.cleancode.image.repository;

import com.example.cleancode.image.entity.Chart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ChartRepository extends MongoRepository<Chart,String> {
    Optional<Chart> findById(String id);
    List<Chart> findByArtist(String artist);
    Optional<Chart> findByTitle(String title);
}
