package com.example.cleancode.song.controller;

import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.song.service.MelonCrawlService;
import com.example.cleancode.aws.service.S3UploadService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
@RequiredArgsConstructor
public class SongController {
    private final MelonCrawlService melonService;
    private final SongRepository songRepository;
    private final S3UploadService s3UploadService;

    @GetMapping("/chartjson")
    @ResponseBody
    public List<Song> giveJson(){
        List<Song> list = songRepository.findByTopTrue();
        return list;
    }

    @GetMapping("/search")
    @ResponseBody
    public List<SongDto> getList2(@RequestParam String target, @RequestParam @Nullable String mode){
        try{
            log.info("아티스트명에서");
            return melonService.search_artist(target, Objects.requireNonNullElse(mode, "0"));
        }catch(Exception ex){
            log.info("decode err: "+ex.toString());
        }
        return null;
    }
    @Deprecated
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
        String path = "/home/ubuntu/2023-2/paran/song_server/src/main/resources/static/";
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
                    List<SongDto> songlist = melonService.search_artist(artist,"1");

                    List<Long> likeString=songlist.stream()
                            .map(SongDto::getLikeId)
                            .collect(Collectors.toList());
                    System.out.println("likeString = " + likeString);
                    Thread.sleep(2000);
                    JSONObject jsonObject = melonService.getLikeNum(likeString);
                    JSONArray contsLikeArray = jsonObject.getJSONArray("contsLike");
                    for(int i=0;i<contsLikeArray.length();i++){
                        JSONObject contsLikeObject = contsLikeArray.getJSONObject(i);
                        System.out.println("contsLikeObject = " + contsLikeObject);
                        String likeId = String.valueOf(contsLikeObject.getInt("CONTSID"));

                        int sumCnt = contsLikeObject.getInt("SUMMCNT");
                        likeIDSumCntMap.put(likeId,sumCnt);
                    }
                    for(SongDto songDto :songlist){
                        if(songDto.getTitle().contains("Inst")|| songDto.getTitle().contains("inst")||
                                songDto.getTitle().contains("Feat")|| songDto.getTitle().contains("feat")|| songDto.getTitle().contains("MR")){
                            log.info("제외된 제목 : {}", songDto.getTitle());
                            continue;
                        } else if (!songDto.getArtist().equals(artist)) {
                            log.info("제외된 가수 : {}", songDto.getArtist());
                            continue;
                        }
                        songDto.setTitle(songDto.getTitle().replace(","," "));
                        Long likeId = songDto.getLikeId();
                        Integer sumCnt = likeIDSumCntMap.get(likeId);
                        if(sumCnt!=null){
                            String genreUrl = "https://www.melon.com/song/detail.htm?songId=";
                            List<String> genreList = new ArrayList<>();
                            Long getGenreParam = songDto.getId();

                            try {
                                Document genreDoc = Jsoup.connect(genreUrl + getGenreParam).get();
                                String genre = genreDoc.select("div.meta dd").eq(2).text();
                                genre = genre.replace(", "," ");
                                System.out.println("genre = " + genre);
                                //제목,가수,장르,좋아요

                                Thread.sleep(2500);
                                String csvRow = songDto.getTitle()+","+ songDto.getArtist()+","+sumCnt+","+genre;
                                log.info(csvRow);
                                writer.write(csvRow);
                                writer.newLine();

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
    @PostMapping("/upload_song")
    public ResponseEntity<Object> uploadSong(@RequestBody MultipartFile file){
        String title = file.getOriginalFilename();
        return ResponseEntity.ok().build();
    }

}
