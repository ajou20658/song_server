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
import com.example.cleancode.utils.CustomException.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserSongRepository userSongRepository;
    private final SongRepository songRepository;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;

    public UserDto findMember(Long userId){
        User member = validator.userValidator(userId);
        return member.toMemberDto();
    }

    @Transactional
    public ProgressStatus userUploadCheck(Long userId,Long songId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        return userSong.getStatus();
    }

    //folder 이름 형식 : user/userId_songId
    @Transactional
    public boolean userFileUpload(MultipartFile multipartFile,Long userId,Long songId){ //동일한 곡에 대해 여러값이 들어가는오류
        User user = validator.userValidator(userId);
        Song song = validator.songValidator(songId);

        Optional<UserSong> userSongOptional = userSongRepository.findByUserIdAndSongId(userId,songId);
        UUID uuid = null;
        if(userSongOptional.isEmpty()){
            uuid = UUID.randomUUID();
        }

        log.info("File type : {}",multipartFile.getContentType());
        String type = multipartFile.getContentType();

        if(!Objects.requireNonNull(type).contains("mpeg")){
            throw new FormatException(ExceptionCode.FORMAT_ERROR);
        }
        String filename = "origin/"+ uuid;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        UserSong userSong = UserSong.builder()
                .originUrl(filename)
                .user(user)
                .song(song)
                .status(ProgressStatus.COMPLETE)
                .build();
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        if(userSongOptional.isPresent()){
            return true;
        }
        userSongRepository.save(userSong);
        return true;
    }


    @Transactional
    public boolean preprocessStart(Long songId, Long userId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        //---------전처리 시작 UserSong Status변경
        try {
            //전처리 요청
            djangoRequest(userSong);
            log.info("django 요청 ");
        } catch (Exception ex){
            userSong.changeStatus(ProgressStatus.ERROR);
            userSongRepository.save(userSong);
            return false;
        }
        return true;
    }
    @Async
    @Transactional
    public void djangoRequest(UserSong userSong){
        String uuid = userSong.getOriginUrl().split("/")[1];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
        body.add("fileKey",userSong.getOriginUrl());
        body.add("isUser","true");
        body.add("uuid",uuid);
        log.info(body.toString());
        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body,headers);

        String url = djangoUrl + "/songssam/post/";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        log.info("status code = {}",response.getStatusCode());
        // userSong Status변경
        userSongRepository.save(userSong.changeStatus(ProgressStatus.COMPLETE));
    }


    @Transactional
    public boolean changeSelectList(List<Long> song,Long userId){
        User user = validator.userValidator(userId);
        UserDto userDto = user.toMemberDto();
        userDto.setSelected(song);
        userRepository.save(userDto.makeMember());
        return true;
    }
    public List<UserSongDto> readUserSongList(Long userId){
        List<UserSong> list = userSongRepository.findByUserId(userId);
        List<UserSongDto> result = new ArrayList<>();
        UserSongDto userSongDto = new UserSongDto();
        for(UserSong song1: list){
            userSongDto.setAwsUrl(song1.getAwsUrl());
            userSongDto.setSpectr(song1.getSpectr());
            userSongDto.setOriginUrl(song1.getOriginUrl());
            userSongDto.setStatus(song1.getStatus());
            userSongDto.setSongId(song1.getSong().getId());
            result.add(userSongDto);
        }
        return result;
    }
    @Transactional
    public void userFileDelete(Long songId,Long userId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        if(!userSong.getAwsUrl().isEmpty()){
            amazonS3.deleteObject(bucket,userSong.getAwsUrl());
        }
        if(!userSong.getOriginUrl().isEmpty()){
            amazonS3.deleteObject(bucket,userSong.getOriginUrl());
        }
        userSongRepository.delete(userSong);
    }
    public List<Song> userLikeSongList(Long userId){
        User user = validator.userValidator(userId);
        List<Long> songNumList = user.getSelected();
        List<Song> songList = new ArrayList<>();
        for(Long i:songNumList){
            songList.add(validator.songValidator(i));
        }
        return songList;
    }
}
