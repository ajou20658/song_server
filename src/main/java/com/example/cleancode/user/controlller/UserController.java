package com.example.cleancode.user.controlller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.service.MemberRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

@RestController
@Slf4j
public class UserController {
    @Autowired
    private MemberRequest memberRequest;
    @Autowired
    private MemberRepository memberRepository;
    @Value("${file.dir}")
    private String fileDir;
    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 반환
     * @param request
     * @return
     */
    @GetMapping("/member")
    public ResponseEntity<MemberDto> memberinfo(HttpServletRequest request){
        MemberDto member = memberRequest.findMember(request);
        log.info(member.toString());
        return ResponseEntity.ok(member);
    }
    /**
     * 쿠키값에 저장된 jwt 토큰을 기반으로 유저 검색후 유저 선호 장르 업데이트
     * @param artist
     * @param genre
     * @param title
     * @param request
     */
    @PostMapping("/update_prefer")
    public void preferUpdate(@RequestBody List<String> artist, @RequestBody List<String> genre, @RequestBody List<String> title, HttpServletRequest request){
        MemberDto member = memberRequest.findMember(request);
        Set<String> set_artist = new HashSet<>(artist);
        Set<String> set_genre = new HashSet<>(genre);
        Set<String> set_title = new HashSet<>(title);
        member.setPreference_Genre(set_genre);
        member.setPreference_Singer(set_artist);
        member.setPreference_Title(set_title);
        memberRepository.save(member.makeMember());
    }

    @PostMapping("/upload")
    public void saveFileV1(@RequestParam MultipartFile file, HttpServletRequest request) throws IOException {
        log.info("Multipartfile={}", file);
        try{
            if(!file.isEmpty()){
                String fileName = file.getOriginalFilename();
                //----------------------디렉토리 확인 후 생성
                Path uploadPath = Paths.get(fileDir);
                if(!Files.exists(uploadPath)){
                    Files.createDirectories(uploadPath);
                }
                //----------------------디렉토리 확인 후 생성
                log.info("파일 저장 fileName={}",fileName);
                file.transferTo(new File(fileDir+fileName));
                /*
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(),filePath);
                 */
            }
            log.info("success");
        }catch (Exception ex){
            ex.printStackTrace();
            log.info("failure");
        }
    }


}
