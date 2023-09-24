package com.example.cleancode.user.controller;

import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.utils.Role;
import com.example.cleancode.utils.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository memberRepository;
    private final SongRepository songRepository;
    private final JwtService jwtService;
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
        List<User> members = memberRepository.findAll();

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

}
