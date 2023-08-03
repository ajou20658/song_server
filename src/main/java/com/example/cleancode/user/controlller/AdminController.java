package com.example.cleancode.user.controlller;

import com.example.cleancode.song.service.MelonService;
import com.example.cleancode.user.JpaRepository.JwtRepository;
import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.JpaRepository.TokenRepository;
import com.example.cleancode.user.entity.Jwt;
import com.example.cleancode.user.entity.KakaoToken;
import com.example.cleancode.user.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JwtRepository jwtRepository;
    @Autowired
    private MelonService melonService;

    @GetMapping("/members")
    public String getMemberList(Model model){
        List<Member> members = memberRepository.findAll();

        model.addAttribute("members",members);
        return "member-list";
    }

    @GetMapping("/tokens")
    public String getTokenList(Model model){
        List<KakaoToken> tokens = tokenRepository.findAll();

        model.addAttribute("tokens",tokens);
        return "token-list";
    }
    @GetMapping("/jwt")
    public String getJwt(Model model){
        List<Jwt> lists = jwtRepository.findAll();
        model.addAttribute("jwts",lists);
        return "jwt-list";
    }
    @GetMapping("/do-crawl")
    public @ResponseBody Long crawl(){
        try{
            return melonService.collectMelonSong();
        }catch(Exception ex){
            log.error(ex.toString());
        }
        return 0l;
    }

    /**
     * 아래는 테스트용도 메서드임
     * @return
     */
    @GetMapping("/form")
    public String showUploadForm(){
        return "uploadForm";
    }
    public static final String UPLOAD_DIR = "upload-dir";//절대 경로로
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        // 업로드된 파일 처리 로직
        if (!file.isEmpty()) {
            try {
                String fileName = StringUtils.cleanPath(file.getOriginalFilename());

                Path uploadPath = Paths.get(UPLOAD_DIR);
                if(!Files.exists(uploadPath)){
                    Files.createDirectories(uploadPath);
                }

                Path filePath=uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(),filePath);
            } catch (IOException e) {
//                model.addAttribute("message", "파일 업로드 실패: " + e.getMessage());
            }
        } else {
//            model.addAttribute("message", "파일 업로드 실패: 업로드할 파일이 없습니다.");
            //실패 페이지로 리다이렉트 필요
        }
        return "uploadForm";//이곳은 성공후 리다이렉트 될 페이지
    }
}
