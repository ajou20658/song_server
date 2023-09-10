package com.example.cleancode.user.service;

import com.example.cleancode.user.JpaRepository.FilePathRepository;
import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.FilePathDto;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.FilePath;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.Role;
import com.example.cleancode.utils.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class MemberService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FilePathRepository filePathRepository;
    @Value("${file.dir}")
    private String fileDir;
    public MemberDto findMember(Long id){
        log.info(id.toString());
        Optional<Member> mem = memberRepository.findById(id);
        Member member = mem.get();
        if(mem.isEmpty()) return null;
        return member.toMemberDto();
    }

    public boolean updatePrefer(List<String> artist,List<String> genre,List<String> title, Long id){
        MemberDto member = findMember(id);
        Set<String> set_artist = new HashSet<>(artist);
        Set<String> set_genre = new HashSet<>(genre);
        Set<String> set_title = new HashSet<>(title);
        try{
            memberRepository.save(member.makeMember());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public boolean updateUser(MemberDto memberDto,Long id){
        MemberDto member = findMember(id);
        memberDto.setId(member.getId());
        memberDto.setRole(Role.ROLE_USER);
        try {
            memberRepository.save(memberDto.makeMember());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    @Transactional
    public boolean upload_file(MultipartFile file,String id){
        log.info("Multipartfile={}", file);
        FilePathDto filePathDto = null;
        try{
            if(!file.isEmpty()){
                String fileName = id+"_"+LocalDate.now();
                filePathDto = FilePathDto.builder()
                        .uri(fileName)
                        .build();
                //----------------------디렉토리 확인 후 생성
                Path uploadPath = Paths.get(fileDir);
                if(!Files.exists(uploadPath)){
                    Files.createDirectories(uploadPath);
                }
                //----------------------디렉토리 확인 후 생성
                log.info("파일 저장 fileName={}",fileName);
                file.transferTo(new File(fileDir+fileName));
            }
            filePathRepository.save(filePathDto.filePath());
            log.info("success");
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
            log.info("failure");
            return false;
        }
    }
    public ResponseEntity<Resource> get_file(String id) throws FileNotFoundException {
//         List<FilePath> list = filePathRepository.findByuserId(id);
        String filePath = fileDir+"\\song";
        File file = new File(filePath);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename="+file.getName());
        headers.setContentLength(file.length());
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(mediaType)
                .body(resource);
    }
}
