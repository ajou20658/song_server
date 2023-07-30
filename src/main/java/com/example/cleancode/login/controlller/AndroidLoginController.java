package com.example.cleancode.login.controlller;

import com.example.cleancode.login.JpaRepository.MemberRepository;
import com.example.cleancode.login.entity.Member;
import com.example.cleancode.login.service.AndroidLoginService;
import com.example.cleancode.login.service.oauth.AndroidRequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
//http://3.34.194.47:8080/v1/login
@RestController
@RequestMapping("/v1")
public class AndroidLoginController {
    @Autowired
    private AndroidLoginService androidLoginService;
    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/login")
    public void login(@RequestBody AndroidRequestParam androidRequestParam){
        androidLoginService.join(androidRequestParam);
    }

    @GetMapping("/member")
    public Member member(@RequestBody Long id){
        return memberRepository.findById(id).orElse(null);
    }
}
