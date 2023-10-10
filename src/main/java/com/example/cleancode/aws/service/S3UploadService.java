package com.example.cleancode.aws.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.example.cleancode.aws.entity.UploadStatus;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.UserSongDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.NoSongException;
import com.example.cleancode.utils.CustomException.NoUserException;
import com.example.cleancode.utils.CustomException.NoUserSongException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 amazonS3;
    private final SongRepository songRepository;
    private final UserSongRepository userSongRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final Map<String,UploadStatus> userUploadStatusMap;

    private final String url = "http://localhost:8000/songssam/post";

    public Resource stream(String url){
        S3Object s3Object = amazonS3.getObject(bucket,url);
        return new InputStreamResource(s3Object.getObjectContent());
    }
    //input : 원곡
    //logic : vocal + instru
    @Async
    public void split(String taskId ,byte[] bs, Long songId, @Nullable Long userId) throws IOException {
        try {
            //전역변수 갱신
            UploadStatus uploadStatus = userUploadStatusMap.get(taskId);
            uploadStatus.setStatus("IN_PROGRESS");
            uploadStatus.setExpectTime(600L);
            userUploadStatusMap.put(taskId,uploadStatus);
            String uuid = UUID.randomUUID().toString();
            //헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(bs));
            body.add("isUser", true);
            body.add("songId", songId);
            body.add("uuid",uuid);
            if (userId != null) {
                body.add("userId", userId);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            //응답 받기
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            log.info("status code = {}", response.getStatusCode());
            uploadStatus.setStatus("COMPLETE");
            userUploadStatusMap.put(taskId,uploadStatus);


            if(userId==null){
                //노래 전송이라면
                uploadSongWav(songId,uuid);
            }else {
                //사용자 전송이라면
                uploadUserSongWav(songId,uuid,userId);
            }
            log.info("전처리 완료 taskId = {}",taskId);
        }catch (Exception e){
            UploadStatus uploadStatus = new UploadStatus();
            uploadStatus.setStatus("FAILED");
        }
    }
    @Transactional
    public void uploadSongWav(Long songId,String uuid) throws Exception {
        Optional<Song> optionalSong = songRepository.findById(songId);
        SongDto tmp = null;
        if(optionalSong.isPresent()){
            tmp = optionalSong.get().toSongDto();
        } else {
            throw new Exception();
        }
        tmp.setVocalUrl("vocal/"+uuid+"_"+songId);
        tmp.setInstUrl("inst/"+uuid+"_"+songId);
        songRepository.save(tmp.toSongEntity());
    }
    private void uploadUserSongWav(Long songId,String uuid,Long userId){
        Optional<UserSong> optionalUserSong = userSongRepository.findByUserIdAndSongId(userId,songId);
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Song> optionalSong = songRepository.findById(songId);
        if(optionalSong.isEmpty()){
            throw new NoSongException(ExceptionCode.SONG_INVALID);
        }
        if(optionalUser.isEmpty()){
            throw new NoUserException(ExceptionCode.USER_INVALID);
        }
        UserSongDto tmp = null;
        if(optionalUserSong.isPresent()){
            tmp = optionalUserSong.get().toUserSongDto();
        }else {

            tmp = UserSong.builder()
                    .song(optionalSong.get())
                    .user(optionalUser.get())
                    .build().toUserSongDto();
        }
        tmp.setAwsUrl("user/"+uuid+"_"+songId);
    }
    @Deprecated
    private boolean vocalUpload(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String[] concat = originalFilename.split("_");
        Optional<Song> optionalSong = songRepository.findByArtistAndTitle(concat[2],concat[1]);
        if(optionalSong.isEmpty()){
            return false;
        }
        SongDto songDto = optionalSong.get().toSongDto();
        String filename = "vocal/"+UUID.randomUUID().toString()+songDto.getId();
        songDto.setVocalUrl(filename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            songRepository.save(songDto.toSongEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
    @Deprecated
    private boolean instUpload(MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String[] concat = originalFilename.split("_");
        Optional<Song> chart100Optional = songRepository.findByArtistAndTitle(concat[2],concat[1]);
        if(chart100Optional.isEmpty()){
            return false;
        }
        SongDto songDto = chart100Optional.get().toSongDto();
        String filename = "inst/"+ songDto.getId();
        songDto.setInstUrl(filename);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            songRepository.save(songDto.toSongEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
}
