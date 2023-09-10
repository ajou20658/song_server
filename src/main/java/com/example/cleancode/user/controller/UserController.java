package com.example.cleancode.user.controller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.entity.UserPrinciple;
import com.example.cleancode.user.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity<MemberDto> memberinfo(@AuthenticationPrincipal UserPrinciple userPrinciple){
        MemberDto member = memberService.findMember(Long.valueOf(userPrinciple.getId()));
        log.info(member.toString());
        return ResponseEntity.ok(member);
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
    public boolean saveFileV1(@RequestBody MultipartFile file, @AuthenticationPrincipal UserPrinciple userPrinciple) throws IOException {
        //file이용해서 file의 음역대 분석 -> min,max 음역대 추출 min,max는 파일 이름으로 사용할 예정
        return memberService.upload_file(file,userPrinciple.getId());
    }
    @GetMapping("/get_file") //업로드한 파일 보기 -스트리밍형식으로?
    public void getFile(){
        
    }
    @GetMapping("/get_result") //완료 결과 가져오기 -스트리밍형식으로?
    public void getResult(){
        
    }
//    @PostMapping("/file_upload")
//    public void uploadFile(@){
////        memberService.upload_file()
//    }
}
