package com.example.cleancode.ddsp.repository;

import com.example.cleancode.ddsp.entity.PtrData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PtrDataRepository extends JpaRepository<PtrData,Long> {
}
