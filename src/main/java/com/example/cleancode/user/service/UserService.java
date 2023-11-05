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
import com.example.cleancode.user.dto.UserSongOutput;
import com.example.cleancode.user.entity.Dataframe2Json;
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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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
    public boolean userFileUpload(MultipartFile multipartFile,Long userId,Long songId){ //동일한 곡에 대해 여러값이 들어가는 오류
        User user = validator.userValidator(userId);
        Song song = validator.songValidator(songId);

        Optional<UserSong> userSongOptional = userSongRepository.findBySongIdAndUserId(songId,userId);

        log.info("File type : {}",multipartFile.getContentType());
        String type = multipartFile.getContentType();

        if(!Objects.requireNonNull(type).contains("mpeg")){
            throw new FormatException(ExceptionCode.FORMAT_ERROR);
        }
        String filename="";
        if(userSongOptional.isPresent()){   //기존에 존재한경우
            filename = "origin/"+userSongOptional.get().getOriginUrl().split("/")[1];
        }else { //기존에 없었던 경우
            filename = "origin/"+UUID.randomUUID();
        }
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        UserSong userSong = UserSong.builder()
                .awsUrl("")
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
        WebClient webClient = WebClient.create();

//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("fileKey",userSong.getOriginUrl());
        body.add("isUser","true");
        body.add("uuid",uuid);

        log.info(body.toString());

        String url = djangoUrl + "/songssam/splitter/";
//        ResponseEntity<String> response = restTemplate.exchange(
//                url,
//                HttpMethod.POST,
//                requestEntity,
//                String.class
//        );

        webClient.post()
            .uri(url)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono(Dataframe2Json.class)
            .subscribe(response -> {
                log.info("status message = {}",response);
                List<Integer> res = json2List(response);
                UserSongDto userSongDto = userSong.toUserSongDto();
                userSongDto.setSpectr(res);
                userSongDto.setStatus(ProgressStatus.COMPLETE);
                userSongRepository.save(userSongDto.toUserSong());
            });
        // userSong Status변경
//        userSongRepository.save(userSong.changeStatus(ProgressStatus.COMPLETE));
    }
    private List<Integer> json2List(Dataframe2Json rawJson){
        List<Integer> result = new ArrayList<>();
        result.add(rawJson.getF1());
        result.add(rawJson.getF2());
        result.add(rawJson.getF3());
        result.add(rawJson.getF4());
        result.add(rawJson.getF5());
        result.add(rawJson.getF6());
        result.add(rawJson.getF7());
        result.add(rawJson.getF8());
        log.info(result.toString());
        return result;
    }

    @Transactional
    public boolean changeSelectList(List<Long> song,Long userId){
        User user = validator.userValidator(userId);
        UserDto userDto = user.toMemberDto();
        userDto.setSelected(song);
        userRepository.save(userDto.makeMember());
        return true;
    }
    public List<UserSongOutput> readUserSongList(Long userId){
        List<UserSong> list = userSongRepository.findByUserId(userId);
        List<UserSongOutput> result = new ArrayList<>();

        for(UserSong song1: list){
            result.add(song1.toUserSongDto().outputFormat());
        }
        return result;
    }
    @Transactional
    public void userFileDelete(Long songId,Long userId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        if(!userSong.getAwsUrl().isEmpty()){    //분리후 압축 된 것 저장
            log.info("SongId,UserId AwsUrl제거 : {}", userSong.getAwsUrl());
            amazonS3.deleteObject(bucket,userSong.getAwsUrl());
        }
        if(!userSong.getOriginUrl().isEmpty()){ //원본 파일
            log.info("SongId,UserId OriginUrl제거 : {}", userSong.getOriginUrl());
            amazonS3.deleteObject(bucket,userSong.getOriginUrl());
        }
//        if(!userSong.getSpectr().isEmpty()){    //f0 추출값...은 로컬에 저장하기로 했지
//
//        }
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
