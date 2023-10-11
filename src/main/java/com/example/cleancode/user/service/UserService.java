package com.example.cleancode.user.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.dto.UserSongDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.NoUserSongException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserSongRepository userSongRepository;
    private final SongRepository songRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public UserDto findMember(Long id){
        log.info(id.toString());
        Optional<User> mem = userRepository.findById(id);
        if(mem.isEmpty()) return null;
        User member = mem.get();
        return member.toMemberDto();
    }

    @Transactional
    public ProgressStatus userUploadCheck(Long userId,Long songId){
        Optional<UserSong> optionalUserSong = userSongRepository.findByUserIdAndSongId(userId,songId);
        if(optionalUserSong.isEmpty()){
            throw new NoUserSongException(ExceptionCode.USER_SONG_INVALID);
        }
        return optionalUserSong.get().getStatus();
    }

    //folder 이름 형식 : user/userId_songId
    @Transactional
    public boolean userFileUpload(MultipartFile multipartFile,Long userId,Long songId){
        UUID uuid = UUID.randomUUID();
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Song> songOptional = songRepository.findById(songId);
        if(userOptional.isEmpty()|songOptional.isEmpty()){
            throw new NoUserSongException(ExceptionCode.USER_SONG_INVALID);
        }
        log.info("File type : {}",multipartFile.getContentType());
        String type = multipartFile.getContentType();
        if(!type.contains("audio")){
            return false;
        }else {
            if(!type.equals("audio/wav")){
                //이곳에 파일형식 변경 로직 필요
            }
        }
        String filename = "user/"+ uuid;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        Optional<UserSong> userSongOptional = userSongRepository.findByAwsUrl(filename);
        UserSong userSong = userSongOptional.orElseGet(() -> UserSong.builder()
                .originUrl(filename)
                .user(userOptional.get())
                .song(songOptional.get())
                .status(ProgressStatus.PROGRESS)
                .build());
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            userSongRepository.save(userSong);
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
    @Transactional
    public boolean changeSelectList(List<Long> song,Long id){

        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isEmpty())return false;
        UserDto userDto = userOptional.get().toMemberDto();
        userDto.setSelected(song);
        userRepository.save(userDto.makeMember());
        return true;
    }
    public List<UserSong> readUserSongList(Long id){
        List<UserSong> list = userSongRepository.findByUserId(id);
        List<UserSong> result = new ArrayList<>();
        UserSongDto userSongDto = new UserSongDto();
        for(UserSong song1: list){
            userSongDto.setAwsUrl(song1.getAwsUrl());
            userSongDto.setSpectr(song1.getSpectr());
            result.add(userSongDto.toUserSong());
        }
        return  result;
    }
    @Transactional
    public void userFileDelete(String url,Long id){
        amazonS3.deleteObject(bucket,url);
        String[] songId = url.split("_");
        Optional<UserSong> tmp = userSongRepository.findByUserIdAndSongId(id, Long.valueOf(songId[1]));
        if(tmp.isEmpty()){
            throw new NoUserSongException(ExceptionCode.SONG_INVALID);
        }
        userSongRepository.delete(tmp.get());
    }
}
