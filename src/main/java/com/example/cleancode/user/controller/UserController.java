package com.example.cleancode.user.controller;

import com.example.cleancode.song.service.S3UploadService;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.utils.UserPrinciple;
import com.example.cleancode.user.entity.UserSong;
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

    private final UserRepository userRepository;
    private final UserService userService;
    private final S3UploadService s3UploadService;
    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     */
    @GetMapping("/info")
    public ResponseEntity<Object> memberinfo(@AuthenticationPrincipal UserPrinciple userPrinciple){
        UserDto member = userService.findMember(Long.valueOf(userPrinciple.getId()));
        log.info("/member/info 유저 이름 : {}",member.getNickname());
        if(member==null){
            Map<String,Object> response = new HashMap<>();
            response.put("response",member);
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        }
        Map<String,Object> response = new HashMap<>();
        response.put("response",member);
        ResponseEntity result = new ResponseEntity<>(response,HttpStatus.OK);
        log.info(result.getHeaders().toString());
        return result;
    }

    @PostMapping("/user_list_update")
    public ResponseEntity userUpdate(List<Long> songList, @AuthenticationPrincipal UserPrinciple userPrinciple){
        if(userService.changeSelectList(songList, userPrinciple.getId())){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/vocal_upload")
    public ResponseEntity saveFileV1(@RequestBody MultipartFile file, @AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException {
        //file이용해서 file의 음역대 분석 -> min,max 음역대 추출 min,max는 파일 이름으로 사용할 예정
        if(userService.userFileUpload("user",file,userPrinciple.getId())){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();

    }

    @GetMapping("/vocal_list")
    public ResponseEntity<Object> userVocalList(@AuthenticationPrincipal UserPrinciple userPrinciple){
        List<UserSong> list = userRepository.findUserSongById(userPrinciple.getId());
        Map<String,Object> response = new HashMap<>();
        response.put("response",list);
        ResponseEntity result = new ResponseEntity<>(response,HttpStatus.OK);
        return result;
    }
    @GetMapping("/vocal_download")
    @ResponseBody
    public ResponseEntity<Resource> streamWavFile(@RequestParam String url){
        try{
            log.info("String : {}",url);
            Resource resource = s3UploadService.stream(url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/wav"));
            headers.setContentDispositionFormData("inline","audio.wav");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        }catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
