package com.example.cleancode.user.controller;

import com.example.cleancode.song.entity.Chart;
import com.example.cleancode.song.repository.ChartRepository;
import com.example.cleancode.song.service.MelonCrawlService;
import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.user.service.LoginService;
import com.example.cleancode.utils.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private MemberRepository memberRepository;
//    @Autowired
//    private TokenRepository tokenRepository;
    @Autowired
    private ChartRepository chartRepository;
    @Autowired
    private MelonCrawlService melonService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private JwtService jwtService;
    @GetMapping("/generate")
    @ResponseBody
    public JwtDto getJwt(){
        MemberDto memberDto = MemberDto.builder()
                .role(Role.ROLE_USER)
                .id(2919293l)
                .email("kwy1379@naver.com")
                .profile("kwy1379")
                .nickname("kwy1379")
                .build();
        return jwtService.generate(memberDto);
    }
    @GetMapping("/members")
    public String getMemberList(Model model){
        List<Member> members = memberRepository.findAll();

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

    @GetMapping("/do-crawl")
    public @ResponseBody Long crawl(){
        try{
            return melonService.collectMelonSong();
        }catch(Exception ex){
            log.error(ex.toString());
        }
        return 0l;
    }
    @GetMapping("/showall")
    public String getList(Model model){
        List<Chart> charts = chartRepository.findAll();
        model.addAttribute("charts",charts);
        return "chart-list";
    }

    /**
     * 아래는 테스트용도 메서드임
     * @return
     */

}
