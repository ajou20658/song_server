package com.example.cleancode.song.controller;

import com.example.cleancode.aws.service.S3UploadService;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.dto.SongFormat;
import com.example.cleancode.song.dto.SongOutput;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.song.service.MelonCrawlService;
import com.example.cleancode.song.service.VocalPreProcessService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
    private final VocalPreProcessService vocalPreProcessService;

    @GetMapping("/chartjson")
    @ResponseBody
    public List<Song> giveJson(){
        return songRepository.findByIsTop(true);
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

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Object> uploadSong(@RequestPart("file") MultipartFile multipartFile, @RequestParam Long songId){
        if(vocalPreProcessService.songUpload(multipartFile,songId)){
            Map<String,Object> response = new HashMap<>();
            response.put("response",songId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().build();
    }
    @GetMapping("/available_list")
    @ResponseBody
    public List<Song> giveAvailalbleList(){
        List<Song> song = songRepository.findByOriginUrlIsNotNull();
        return song;
    }
//    @GetMapping("/listen")
//    public ResponseEntity<Object> listenSong(@RequestParam String url) throws IOException {
//        log.info("url : {}",url);
//        Resource resource = s3UploadService.miniStream2(url,60);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
//        headers.setContentDispositionFormData("inline","audio.mp3");
//        headers.setContentLength(resource.contentLength());
//        return ResponseEntity.ok()
//                .headers(headers)
//                .body(resource);
//    }
    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> streamWavFile(@RequestParam String url){
        log.info("String : {}",url);
        Resource resource = s3UploadService.stream(url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentDispositionFormData("inline","audio.mp3");
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    @PostMapping("/preprocess")
    public ResponseEntity<Object> processSong(@RequestParam Long songId){
        log.info("preprocess : {}",songId);
        boolean result = vocalPreProcessService.preprocessStart(songId);
        if(result){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping
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
    @Deprecated
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
                            .map(SongDto::getId)
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
                        Long likeId = songDto.getId();
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

    @GetMapping("/download/csv")
    public ResponseEntity<byte[]> downloadCSV() throws Exception {
        List<Song> data = songRepository.findByOriginUrlIsNotNull();
        //Header
        List<Long> likeList = new ArrayList<>();
        for (Song i: data){
            likeList.add(i.getId());
        }
        Map<String,Integer> likeMap = new HashMap<>();
        log.info(likeList.toString());
        JSONObject jsonObject = melonService.getLikeNum(likeList);
        JSONArray contsLikeArray = jsonObject.getJSONArray("contsLike");
        for(int i=0;i<contsLikeArray.length();i++){
            JSONObject contsLikeObject = contsLikeArray.getJSONObject(i);
            System.out.println("contsLikeObject = " + contsLikeObject);
            String likeId = String.valueOf(contsLikeObject.getInt("CONTSID"));

            int sumCnt = contsLikeObject.getInt("SUMMCNT");
            likeMap.put(likeId,sumCnt);
        }
        List<SongOutput> result = new ArrayList<>();
        for (Song i: data){
            result.add(SongOutput.builder()
                    .id(Math.toIntExact(i.getId()))
                    .like(likeMap.get(i.getId()))
                    .artist(i.getArtist())
                    .title(i.getTitle())
                    .genre(StringUtils.collectionToDelimitedString(i.getGenre()," "))
                    .encodedGenre(StringUtils.collectionToDelimitedString(i.getEncoded_genre()," "))
//                    .f0_1(i.getSpectr().get(0))
//                    .f0_2(i.getSpectr().get(1))
//                    .f0_3(i.getSpectr().get(2))
//                    .f0_4(i.getSpectr().get(3))
//                    .f0_5(i.getSpectr().get(4))
//                    .f0_6(i.getSpectr().get(5))
//                    .f0_7(i.getSpectr().get(6))
//                    .f0_8(i.getSpectr().get(7))
                    .build());
        }
        StringBuilder csvData = new StringBuilder();
        // Data
        for (SongOutput i : result){
            csvData.append(i.getTitle()+","+i.getArtist()+","+i.getLike()+","+i.getGenre()+","+
                    i.getEncodedGenre()+"\n");
            //+","+i.getF0_1()+","+i.getF0_2()+i.getF0_3()+","+i.getF0_4()
            //                    +","+i.getF0_5()+","+i.getF0_6()+","+i.getF0_7()+","+i.getF0_8()
        }
        byte[] csvBytes = csvData.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mydata.csv");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(csvBytes.length)
                .body(csvBytes);
    }

}
