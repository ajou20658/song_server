package com.example.cleancode.user.controller;

import com.example.cleancode.song.service.S3UploadService;
import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.UserPrinciple;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.user.service.MemberService;
import com.example.cleancode.utils.jwt.TokenStatus;
import com.example.cleancode.utils.jwt.TokenValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/member")
public class UserController {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private S3UploadService s3UploadService;
    @Autowired
    private UserSongRepository userSongRepository;
    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     * @param userPrinciple
     * @return
     */
    @GetMapping("/info")
    public ResponseEntity<Object> memberinfo(@AuthenticationPrincipal UserPrinciple userPrinciple){
        MemberDto member = memberService.findMember(Long.valueOf(userPrinciple.getId()));
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
    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 검색후 유저 선호 장르 업데이트
     * 이후 상태코드도 같이 보내주게 변경 필요
     * @param artist
     * @param genre
     * @param title
     * @param userPrinciple
     */
    @PostMapping("/update_prefer")
    public boolean preferUpdate(@RequestBody List<String> artist, @RequestBody List<String> genre, @RequestBody List<String> title, @AuthenticationPrincipal UserPrinciple userPrinciple){

        return memberService.updatePrefer(artist, genre, title, Long.valueOf(userPrinciple.getId()));
    }
    @PostMapping("/update_user")
    public boolean userUpdate(@RequestBody MemberDto memberDto,@AuthenticationPrincipal UserPrinciple userPrinciple){
        return memberService.updateUser(memberDto, Long.valueOf(userPrinciple.getId()));
    }
    @PostMapping("/vocal_upload")
    public ResponseEntity saveFileV1(@RequestBody MultipartFile file, @AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException {
        //file이용해서 file의 음역대 분석 -> min,max 음역대 추출 min,max는 파일 이름으로 사용할 예정
        String url = s3UploadService.userFile("user",file,userPrinciple.getId());
        userSongRepository.save(UserSong.builder()
                        .url(url)
                        .userid(Long.valueOf(userPrinciple.getId()))
                .build());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/vocal_stream")
    public ResponseEntity<Resource> streamWavFile(@RequestParam String url){
        try{
            Resource resource = s3UploadService.stream(url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment","audio.wav");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        }catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/vocal_list")
    public ResponseEntity<Object> userVocalList(@AuthenticationPrincipal UserPrinciple userPrinciple){
        List<UserSong> userSongList = userSongRepository.findByUseridByInsDateDesc(Long.valueOf(userPrinciple.getId()));
        Map<String,Object> response = new HashMap<>();
        response.put("response",userSongList);
        ResponseEntity result = new ResponseEntity<>(response,HttpStatus.OK);
        return result;
    }
    @PostMapping("/aws_upload")
    public ResponseEntity<Object> saveFile(@RequestBody MultipartFile file, @AuthenticationPrincipal UserPrinciple userPrinciple){
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/get_result") //완료 결과 가져오기 -스트리밍형식으로?
    public void getResult(){
        
    }
//    @PostMapping("/file_upload")
//    public void uploadFile(@){
////        memberService.upload_file()
//    }
}
