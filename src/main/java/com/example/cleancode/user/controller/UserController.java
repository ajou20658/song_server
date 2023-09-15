package com.example.cleancode.user.controller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.FilePath;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.entity.UserPrinciple;
import com.example.cleancode.user.service.MemberService;
import com.example.cleancode.utils.jwt.TokenStatus;
import com.example.cleancode.utils.jwt.TokenValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/member")
public class UserController {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;


    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     * @param userPrinciple
     * @return
     */
    @GetMapping("/info")
    public ResponseEntity<Object> memberinfo(HttpServletRequest request,@AuthenticationPrincipal UserPrinciple userPrinciple){
        TokenValidationResult validationResult = (TokenValidationResult) request.getAttribute("result");
        if(validationResult.getTokenStatus() == TokenStatus.TOKEN_EXPIRED){
            Map<String,Object> response = new HashMap<>();
            response.put("HttpStatus",HttpStatus.UNAUTHORIZED);
            response.put("message","재갱신이 필요합니다");
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        }
        MemberDto member = memberService.findMember(Long.valueOf(userPrinciple.getId()));
        log.info(member.toString());
        if(member==null){
            Map<String,Object> response = new HashMap<>();
            response.put("HttpStatus",HttpStatus.FORBIDDEN.value());
            response.put("response",member);
            return new ResponseEntity<>(response,HttpStatus.FORBIDDEN);
        }
        Map<String,Object> response = new HashMap<>();
        response.put("HttpStatus",HttpStatus.OK.value());
        response.put("response",member);
        return new ResponseEntity<>(response,HttpStatus.OK);
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
    public ResponseEntity<Object> saveFileV1(@RequestBody MultipartFile file, @AuthenticationPrincipal UserPrinciple userPrinciple) {
        //file이용해서 file의 음역대 분석 -> min,max 음역대 추출 min,max는 파일 이름으로 사용할 예정
        log.info("file: {}",file.getOriginalFilename());
//        if(memberService.upload_file(file,userPrinciple.getId())){
//            //true일떄
//            Map<String,Object> response = new HashMap<>();
//            response.put("HttpStatus",HttpStatus.OK.value());
//            return new ResponseEntity<>(response,HttpStatus.OK);
//        }
        return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
    }
    @GetMapping("/get_file") //업로드한 파일 보기 -스트리밍형식으로?
    public ResponseEntity<Resource> getFile(@AuthenticationPrincipal UserPrinciple userPrinciple) throws FileNotFoundException {
        return memberService.get_file(userPrinciple.getId());

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
