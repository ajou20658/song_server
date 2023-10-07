package com.example.cleancode.user.JpaRepository;

import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

}
