package com.example.cleancode.login.controlller;

import com.example.cleancode.login.JpaRepository.MemberRepository;
import com.example.cleancode.login.JpaRepository.TokenRepository;
import com.example.cleancode.login.entity.KakaoToken;
import com.example.cleancode.login.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenRepository tokenRepository;

    @GetMapping("/members")
    public String getMemberList(Model model){
        List<Member> members = memberRepository.findAll();

        model.addAttribute("members",members);
        return "/member-list";
    }

    @GetMapping("/tokens")
    public String getTokenList(Model model){
        List<KakaoToken> tokens = tokenRepository.findAll();

        model.addAttribute("tokens",tokens);
        return "/token-list";
    }
}
