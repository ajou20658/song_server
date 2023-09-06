package com.example.cleancode.song.controller;

import com.example.cleancode.song.dto.ChartDTO;
import com.example.cleancode.song.dto.SearchDto;
import com.example.cleancode.song.entity.Chart;
import com.example.cleancode.song.repository.ChartRepository;
import com.example.cleancode.song.repository.SongRepository;
//import com.example.cleancode.image.service.MelonService;
import com.example.cleancode.song.service.MelonCrawlService;
import dev.failsafe.internal.util.RandomDelay;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 구현해야 될 것들...
 * 크롤링 하는 코드(o)
 * 검색 기능 구현
 */
@Controller
@Slf4j
@RequestMapping("/song")
public class SongController {
    @Autowired
    private MelonCrawlService melonService;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private ChartRepository chartRepository;
    @Autowired
    private SongRepository songRepository;


    @GetMapping("/chartjson")
    @ResponseBody
    public List<Chart> giveJson(){
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
    @GetMapping("/artist_list_crawl")
    @ResponseBody
    public void getArtist(){
        try{
            melonService.artistCrawl();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/artist_50Song")
    @ResponseBody
    public void get50SongPerArtist(){
        String path = servletContext.getRealPath("/static/");
        String filePath=path+"artist.txt";
        String newFile =path+"data.csv";
//        String filePath="C:\\Users\\kwy\\Documents\\2023하계\\cleancode\\src\\main\\resources\\static\\artist.txt";
//        String newFile = "C:\\Users\\kwy\\Documents\\2023하계\\cleancode\\src\\main\\resources\\static\\data.csv";
        List<String> lines = new ArrayList<>();
        Map<String,Integer> likeIDSumCntMap;
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), StandardCharsets.UTF_8))){
                String line;
                while((line=br.readLine())!=null){
                    lines.add(line);
                }
                for(String artist : lines){
                    likeIDSumCntMap = new HashMap<>();
                    List<SearchDto> songlist = melonService.search_artist(artist,"1");
                    List<String> likeString=songlist.stream()
                            .map(SearchDto::getLikeId)
                            .collect(Collectors.toList());
                    Thread.sleep(1000);
                    JSONObject jsonObject = melonService.getLikeNum(likeString);
                    JSONArray contsLikeArray = jsonObject.getJSONArray("contsLike");
                    for(int i=0;i<contsLikeArray.length();i++){
                        JSONObject contsLikeObject = contsLikeArray.getJSONObject(i);
                        String likeId = String.valueOf(contsLikeObject.getInt("CONTSID"));

                        int sumCnt = contsLikeObject.getInt("SUMMCNT");
                        likeIDSumCntMap.put(likeId,sumCnt);
                    }
                    for(SearchDto searchDto:songlist){
                        String likeId = searchDto.getLikeId();
                        Integer sumCnt = likeIDSumCntMap.get(likeId);
                        if(sumCnt!=null){
                            String genreUrl = "https://www.melon.com/song/detail.htm?songId=";
                            List<String> genreList = new ArrayList<>();
                            String getGenreParam = searchDto.getSongId();
                            try {
                                Document genreDoc = Jsoup.connect(genreUrl + getGenreParam).get();
                                String genre = genreDoc.select("div.meta dd").eq(2).text();

                                //제목,가수,장르,좋아요
                                if(searchDto.getTitle().contains("Inst")||searchDto.getTitle().contains("inst")||searchDto.getTitle().contains("Feat")){
                                    Thread.sleep(2000);
                                    log.info("제외된 제목 : {}",searchDto.getTitle());
                                } else if (!searchDto.getArtist().equals(artist)) {
                                    Thread.sleep(1500);
                                    log.info("제외된 제목, 가수 : {} {}",searchDto.getTitle(),searchDto.getArtist());
                                }else{
                                    Thread.sleep(2500);
                                    String csvRow = searchDto.getTitle()+","+searchDto.getArtist()+","+sumCnt+","+"\""+genre+"\"";
                                    log.info(csvRow);
                                    writer.write(csvRow);
                                    writer.newLine();
                                }
                            }catch(RuntimeException ex){
                                log.info("예외 발생 songId = {}",getGenreParam);
                            }

                        }
                    }

                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
