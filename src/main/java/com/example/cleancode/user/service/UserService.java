package com.example.cleancode.user.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.dto.UserSongDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
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
    private final UserRepository memberRepository;
    private final UserSongRepository userSongRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public UserDto findMember(Long id){
        log.info(id.toString());
        Optional<User> mem = memberRepository.findById(id);
        if(mem.isEmpty()) return null;
        User member = mem.get();
        return member.toMemberDto();
    }

    public boolean updateUser(UserDto memberDto, Long id){
        UserDto member = findMember(id);
        memberDto.setId(member.getId());
        memberDto.setRole(Role.ROLE_USER);
        try {
            memberRepository.save(memberDto.makeMember());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    @Transactional
    public boolean userFileUpload(String folder, MultipartFile multipartFile,Long id){
        String originalFilename = multipartFile.getOriginalFilename();
        Optional<User> userOptional = memberRepository.findById(id);
        if(userOptional.isEmpty()){
            return false;
        }

        String filename = folder+"/"+id+"_"+originalFilename;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        UserSong userSong = UserSong.builder()
                .awsUrl(filename)
                .user(userOptional.get())
                .build();
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

        Optional<User> userOptional = memberRepository.findById(id);
        if(userOptional.isEmpty())return false;
        UserDto userDto = userOptional.get().toMemberDto();
        userDto.setSelected(song);
        memberRepository.save(userDto.makeMember());
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

}
