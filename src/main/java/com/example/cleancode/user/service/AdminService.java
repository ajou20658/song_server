package com.example.cleancode.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.utils.CustomException.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final SongRepository songRepository;
    private final UserSongRepository userSongRepository;
    private final UserRepository userRepository;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    public String deleteUser(Long userId){
        User user = validator.userValidator(userId);
        List<UserSong> userSongList = userSongRepository.findByUserId(userId);
        for(UserSong i:userSongList){
            String vocalUrl = i.getVocalUrl();
            String originUrl = i.getOriginUrl();
            amazonS3.deleteObject(bucket,vocalUrl);
            amazonS3.deleteObject(bucket,originUrl);
            userSongRepository.delete(i);
        }
        userRepository.delete(user);
        return user.getNickname();
    }
}
