package com.example.cleancode.login.JpaRepository;

import com.example.cleancode.login.dto.MemberDto;
import com.example.cleancode.login.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findById(Long id);

}
