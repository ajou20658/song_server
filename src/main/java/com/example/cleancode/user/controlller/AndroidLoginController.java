package com.example.cleancode.user.controlller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.service.AndroidLoginService;
import com.example.cleancode.user.service.oauth.AndroidRequestParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @PostMapping("/update_prefer")
    public void preferUpdate(@RequestBody List<String> artist, @RequestBody List<String> genre, @RequestBody List<String> title, @RequestBody String id){
        Member memberE = memberRepository.findById(Long.parseLong(id)).get();
        MemberDto member = MemberDto.builder()
                .email(memberE.getEmail())
                .nickname(memberE.getNickname())
                .id(memberE.getId())
                .role(memberE.getRole())
                .build();
        Set<String> set_artist = new HashSet<>(artist);
        Set<String> set_genre = new HashSet<>(genre);
        Set<String> set_title = new HashSet<>(title);
        member.setPreference_Genre(set_genre);
        member.setPreference_Singer(set_artist);
        member.setPreference_Title(set_title);
        memberRepository.save(member.makeMember());
    }
}
