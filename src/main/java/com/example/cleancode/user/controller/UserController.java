package com.example.cleancode.user.controller;

import com.example.cleancode.aws.service.S3UploadService;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.utils.UserPrinciple;
import com.example.cleancode.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserService userService;
    private final S3UploadService s3UploadService;

    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     */
    @GetMapping("/info")
    public ResponseEntity<Object> memberinfo(@AuthenticationPrincipal UserPrinciple userPrinciple){
        UserDto user = userService.findMember(userPrinciple.getId());
//        log.info("/member/info 유저 이름 : {}",user.getNickname());
        Map<String,Object> response = new HashMap<>();
        response.put("response",user);
//        log.info(result.getHeaders().toString());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/user_list")
    public ResponseEntity<Object> userUpdate(@RequestBody List<Long> songList, @AuthenticationPrincipal UserPrinciple userPrinciple){
        if(userService.reIssueRecommandList(songList, userPrinciple.getId())){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/user_list")
    public ResponseEntity<Object> user(@AuthenticationPrincipal UserPrinciple userPrinciple){
        List<Song> songList = userService.userLikeSongList(userPrinciple.getId());
        Map<String,Object> response = new HashMap<>();
        response.put("response",songList);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
    @GetMapping("/user_recommand_list")
    @ResponseBody
    public List<Song> userRecommandList(@AuthenticationPrincipal UserPrinciple userPrinciple){
        return userService.userLikeSongWithRecommand(userPrinciple.getId());
    }
    @PostMapping("/upload")
    public ResponseEntity<Object> uploadFile(@RequestPart("file") MultipartFile file,@RequestParam Long songId, @AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException {
        log.info("Voice Upload Req");
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
        boolean result = userService.preprocessStart(songId,userPrinciple.getId());
        if(result){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/vocal_list")
    @ResponseBody
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
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentDispositionFormData("inline","audio.mp3");
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    @PostMapping("/deleteVocalFile")
    @ResponseBody
    public ResponseEntity<Object> vocalDelete(@RequestParam Long songId, @AuthenticationPrincipal UserPrinciple userPrinciple){
        userService.userFileDelete(songId, userPrinciple.getId());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/upload")
    @ResponseBody
    public ResponseEntity<Object> uploadCheck(@RequestParam Long songId, @AuthenticationPrincipal UserPrinciple userPrinciple){
        ProgressStatus result = userService.userUploadCheck(userPrinciple.getId(),songId);
        Map<String,String> response = new HashMap<>();
        response.put("response",result.toString());
        return  new ResponseEntity<>(response,HttpStatus.OK);
    }
}
