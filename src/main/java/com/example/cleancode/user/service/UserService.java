package com.example.cleancode.user.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.aws.entity.UploadStatus;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.dto.UserSongDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.NoUserException;
import com.example.cleancode.utils.CustomException.NoUserSongException;
import com.example.cleancode.utils.Role;
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
    private Map<String, UploadStatus> userUploadStatusMap;

    public UserDto findMember(Long id){
        log.info(id.toString());
        Optional<User> mem = userRepository.findById(id);
        if(mem.isEmpty()) return null;
        User member = mem.get();
        return member.toMemberDto();
    }
    @Transactional
    public String userUploadCheck(String taskId){
        UploadStatus tmp = userUploadStatusMap.get(taskId);
        if(tmp.getStatus().equals("COMPLETE")){
            userUploadStatusMap.remove(taskId);
        }
        return tmp.getStatus();
    }
    //folder 이름 형식 : user/userId_songId
    @Transactional
    public boolean userFileUpload(String folder, MultipartFile multipartFile,Long id){
        String originalFilename = multipartFile.getOriginalFilename();
        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isEmpty()){
            return false;
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
        String filename = folder+"/"+id+"_"+originalFilename;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        Optional<UserSong> userSongOptional = userSongRepository.findByAwsUrl(filename);
        UserSong userSong = userSongOptional.orElseGet(() -> UserSong.builder()
                .awsUrl(filename)
                .user(userOptional.get())
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
            userSongDto.setCreatedAt(song1.getCreatedAt());
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
