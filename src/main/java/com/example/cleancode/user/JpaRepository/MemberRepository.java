package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByNickname(String email);
    Optional<Member> findByid(Long id);

}
