package com.example.cleancode.image.controller;

import com.example.cleancode.image.entity.Chart;
import com.example.cleancode.image.entity.Song;
import com.example.cleancode.image.repository.SongRepository;
//import com.example.cleancode.image.service.MelonService;
import com.example.cleancode.image.service.MelonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/v3")
public class ImageController {
    @Autowired
    private MelonService melonService;

    @Autowired
    private SongRepository songRepository;

    @GetMapping("/form")
    public String showUploadForm(){
        return "uploadForm";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        // 업로드된 파일 처리 로직
        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                file.transferTo(new File("/path/to/upload/directory/" + fileName));
                model.addAttribute("message", "파일 업로드 성공: " + fileName);
            } catch (IOException e) {
                model.addAttribute("message", "파일 업로드 실패: " + e.getMessage());
            }
        } else {
            model.addAttribute("message", "파일 업로드 실패: 업로드할 파일이 없습니다.");
        }
        return "uploadForm";
    }
    @PostMapping("/add-song")
    public void login(@RequestParam Song song){
        songRepository.save(song);
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

    @GetMapping("/showall")
    public String getList(Model model){
//        List<Song> songs = songRepository.findAll();
//        model.addAttribute("songs",songs);
//        List<Chart> charts =
        return "/song-list";
    }
}
