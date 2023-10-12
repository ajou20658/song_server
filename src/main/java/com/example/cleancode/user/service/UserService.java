package com.example.cleancode.user.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.aws.service.S3UploadService;
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
import com.example.cleancode.utils.CustomException.DjangoRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
    private final S3UploadService s3UploadService;
    private final AmazonS3 amazonS3;
    private final RestTemplate restTemplate;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final String url = "http://localhost:8000/songssam/post/";
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
        String filename = "origin/"+ uuid;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        Optional<UserSong> userSongOptional = userSongRepository.findByAwsUrl(filename);
        UserSong userSong = userSongOptional.orElseGet(() -> UserSong.builder()
                .originUrl(filename)
                .user(userOptional.get())
                .song(songOptional.get())
                .status(ProgressStatus.UPLOADED)
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
    public boolean preprocessUpload(Long songId, Long userId){
        Optional<UserSong> optionalUserSong = userSongRepository.findByUserIdAndSongId(userId,songId);
        if(optionalUserSong.isEmpty()){
            throw new NoUserSongException(ExceptionCode.USER_SONG_INVALID);
        }
        UserSong userSong = optionalUserSong.get();
        //---------전처리 시작 UserSong Status변경
        try {
            //전처리 요청
            djangoRequest(userSong);
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
        body.add("isUser",true);
        body.add("uuid",uuid);

        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body,headers);

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
    public boolean changeSelectList(List<Long> song,Long id){

        Optional<User> userOptional = userRepository.findById(id);
        if(userOptional.isEmpty())return false;
        UserDto userDto = userOptional.get().toMemberDto();
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
        Optional<UserSong> optionalUserSong = userSongRepository.findByUserIdAndSongId(userId,songId);
        if(optionalUserSong.isEmpty()){
            throw new NoUserSongException(ExceptionCode.USER_SONG_INVALID);
        }
        UserSong userSong = optionalUserSong.get();
        if(!userSong.getAwsUrl().isEmpty()){
            amazonS3.deleteObject(bucket,userSong.getAwsUrl());
        }
        if(!userSong.getOriginUrl().isEmpty()){
            amazonS3.deleteObject(bucket,userSong.getOriginUrl());
        }
        userSongRepository.delete(userSong);
    }
}
