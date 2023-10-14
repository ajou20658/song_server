package com.example.cleancode.user.controller;

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
import com.example.cleancode.utils.CustomException.NoSongException;
import com.example.cleancode.utils.UserPrinciple;
import com.example.cleancode.user.service.UserService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final UserSongRepository userSongRepository;
    private final UserService userService;
    private final S3UploadService s3UploadService;

    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     */
    @GetMapping("/info")
    public ResponseEntity<Object> memberinfo(@AuthenticationPrincipal UserPrinciple userPrinciple){
        UserDto member = userService.findMember(userPrinciple.getId());
        log.info("/member/info 유저 이름 : {}",member.getNickname());
        Map<String,Object> response = new HashMap<>();
        response.put("response",member);
        ResponseEntity<Object> result = new ResponseEntity<>(response,HttpStatus.OK);
        log.info(result.getHeaders().toString());
        return result;
    }

    @PostMapping("/user_list")
    public ResponseEntity<Object> userUpdate(@RequestBody List<Long> songList, @AuthenticationPrincipal UserPrinciple userPrinciple){
        if(userService.changeSelectList(songList, userPrinciple.getId())){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/user_list")
    public ResponseEntity<Object> user(@AuthenticationPrincipal UserPrinciple userPrinciple){
        Optional<User> userOptional = userRepository.findById(userPrinciple.getId());
        if(userOptional.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        Map<String,Object> response = new HashMap<>();
        response.put("response",userOptional.get().getSelected());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam Long songId, @AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException {
        if(userService.userFileUpload(file,userPrinciple.getId(),songId)){
            Map<String,Object> response = new HashMap<>();
            response.put("response",songId);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
        return ResponseEntity.badRequest().build();
        //----------------------------------------------------------------------------------------
    }
    @PostMapping("/preprocess")
    public ResponseEntity<Object> djangoRequest(@RequestParam Long songId, @AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException{
        boolean result = userService.preprocessUpload(songId,userPrinciple.getId());
        if(result){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/vocal_list")
    public ResponseEntity<Object> userVocalList(@AuthenticationPrincipal UserPrinciple userPrinciple){
        Map<String,Object> response = new HashMap<>();
        response.put("response",userService.readUserSongList(userPrinciple.getId()));
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> streamWavFile(@RequestParam String url){
        log.info("String : {}",url);
        Resource resource = s3UploadService.stream(url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.setContentDispositionFormData("inline","audio.wav");
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<Object> vocalDelete(@RequestBody Long songId, @AuthenticationPrincipal UserPrinciple userPrinciple){
        userService.userFileDelete(songId, userPrinciple.getId());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/upload")
    @ResponseBody
    public ResponseEntity<Object> uploadCheck(@RequestParam Long sondId, @AuthenticationPrincipal UserPrinciple userPrinciple){
        ProgressStatus result = userService.userUploadCheck(userPrinciple.getId(),sondId);
        Map<String,String> response = new HashMap<>();
        response.put("response",result.toString());
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }
}
