package com.example.cleancode.user.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.example.cleancode.ddsp.service.TrainService;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.service.AdminService;
import com.example.cleancode.utils.Role;
import com.example.cleancode.utils.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final TrainService trainService;
    private final JwtService jwtService;
    private final AdminService adminService;
    private final AmazonS3 amazonS3;
    @GetMapping("/generate")
    @ResponseBody
    public JwtDto getJwt(){
        UserDto memberDto = UserDto.builder()
                .role(Role.ROLE_USER)
                .id(2919293L)
                .email("kwy1379@naver.com")
                .profileUrl("kwy1379")
                .nickname("kwy1379")
                .build();
        return jwtService.generate(memberDto);
    }
    @GetMapping("/members")
    public String getMemberList(Model model){
        List<User> members = userRepository.findAll();

        model.addAttribute("members",members);
        return "member-list";
    }

//    @GetMapping("/tokens")
//    public String getTokenList(Model model){
//        List<KakaoToken> tokens = tokenRepository.findAll();
//
//        model.addAttribute("tokens",tokens);
//        return "token-list";
//    }
//    @GetMapping("/jwt")
//    public String getJwt(Model model){
//        List<Jwt> lists = jwtRepository.findAll();
//        model.addAttribute("jwts",lists);
//        return "jwt-list";
//    }

    @GetMapping("/showall")
    public String getList(Model model){
        List<Song> charts = songRepository.findAll();
        model.addAttribute("charts",charts);
        return "chart-list";
    }
    @PostMapping("/upload_ptr")
    public ResponseEntity<Object> uploadPtr(
            @RequestPart("file")MultipartFile file,
            @RequestParam String name){
        trainService.ptrFileUplaod(file,name);
        return ResponseEntity.ok().build();
    }
//    @DeleteMapping("/delete_song")
//    public Response<Object> deleteSong(
//            @RequestParam Integer songId
//    ){
//        songRepository.de
//    }
    @PostMapping("/delete_all_User")
    public ResponseEntity<Object> deleteUserData(){
        List<User> allUser = userRepository.findAll();
        for(User i:allUser){
            String name = adminService.deleteUser(i.getId());
            log.info("삭제 성공 : {}", name);
        }
        return ResponseEntity.ok().build();
    }
}
