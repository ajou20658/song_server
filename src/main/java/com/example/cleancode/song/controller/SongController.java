package com.example.cleancode.song.controller;

import com.example.cleancode.song.dto.SearchDto;
import com.example.cleancode.song.entity.Chart;
import com.example.cleancode.song.repository.ChartRepository;
import com.example.cleancode.song.repository.SongRepository;
//import com.example.cleancode.image.service.MelonService;
import com.example.cleancode.song.service.MelonService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 구현해야 될 것들...
 * 크롤링 하는 코드(o)
 * 검색 기능 구현
 */
@Controller
@Slf4j
@RequestMapping("/v3")
public class SongController {
    @Autowired
    private MelonService melonService;
    @Autowired
    private ChartRepository chartRepository;
    @Autowired
    private SongRepository songRepository;

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

    @GetMapping("/showall")
    public String getList(Model model){
//        List<Song> songs = songRepository.findAll();
//        model.addAttribute("songs",songs);
        List<Chart> charts = chartRepository.findAll();
        model.addAttribute("charts",charts);
        return "/chart-list";
    }
    @GetMapping("/chartjson")
    @ResponseBody
    public List<Chart> giveList(){
        return chartRepository.findAll();
    }

    @GetMapping("/search")
    @ResponseBody
    public List<SearchDto> getList2(@RequestParam @Nullable String artist, @RequestParam String mode, Model model){
        try{
            String decodedArtist = URLDecoder.decode(artist, "UTF-8");

            System.out.println("artist = " + decodedArtist);
            System.out.println("한글 안돼나");
            System.out.println("mode = " + mode);
            switch (mode){
                case "0":
                    log.info("전체에서");
                case "1":
                    log.info("아티스트명에서");
                    try {
                        return melonService.search_artist(artist);
                    }catch (Exception ex){
                        log.info(ex.toString());
                    }
                case "2":
                    log.info("곡명에서");
                case "3":
                    log.info("앨범명에서");
            }
        }catch(Exception ex){
            log.info("decode err: "+ex.toString());
        }
        return null;
    }


}
