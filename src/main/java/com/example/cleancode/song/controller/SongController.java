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
    public List<SearchDto> getList2(@RequestParam @Nullable String target, @RequestParam String mode, Model model){
        try{
            String decodedArtist = URLDecoder.decode(target, "UTF-8");
            log.info("아티스트명에서");
            return melonService.search_artist(target,mode);
        }catch(Exception ex){
            log.info("decode err: "+ex.toString());
        }
        return null;
    }


}
