package com.example.cleancode.song.service;

import com.example.cleancode.song.dto.SongFormat;
import com.example.cleancode.song.dto.SongOutput;
import com.example.cleancode.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ToCSVService {
    private final SongRepository songRepository;
    private final MelonCrawlService melonCrawlService;

    @GetMapping("/download/csv")
    public ResponseEntity<String> downloadCSV(HttpServletResponse response) throws Exception {

        List<SongFormat> data = songRepository.findPartialData();

        //Header
//        ResultSet resultSet = 쿼리로 가져오기
        List<Long> likeList = new ArrayList<>();
        for (SongFormat i: data){
            likeList.add(i.getId());
        }
        HashMap<String,Integer> likeMap = new HashMap<>();

        JSONObject jsonObject = melonCrawlService.getLikeNum(likeList);
        JSONArray contsLikeArray = jsonObject.getJSONArray("contsLike");
        for(int i=0;i<contsLikeArray.length();i++){
            JSONObject contsLikeObject = contsLikeArray.getJSONObject(i);
            System.out.println("contsLikeObject = " + contsLikeObject);
            String likeId = String.valueOf(contsLikeObject.getInt("CONTSID"));

            int sumCnt = contsLikeObject.getInt("SUMMCNT");
            likeMap.put(likeId,sumCnt);
        }
        List<SongOutput> result = new ArrayList<>();
        for (SongFormat i: data){
            result.add(SongOutput.builder()
                .id(Math.toIntExact(i.getId()))
                .like(likeMap.get(i.getId()))
                .artist(i.getArtist())
                .title(i.getTitle())
                .genre(StringUtils.collectionToDelimitedString(i.getGenre()," "))
                .encodedGenre(StringUtils.collectionToDelimitedString(i.getEncoded_genre()," "))
                .f0_1(i.getSpectr().get(0))
                .f0_2(i.getSpectr().get(1))
                .f0_3(i.getSpectr().get(2))
                .f0_4(i.getSpectr().get(3))
                .f0_5(i.getSpectr().get(4))
                .f0_6(i.getSpectr().get(5))
                .f0_7(i.getSpectr().get(6))
                .f0_8(i.getSpectr().get(7))
                .build());
        }
        StringBuilder csvData = new StringBuilder();
        // Data
        for (SongOutput i : result){
            csvData.append(i.getTitle()+","+i.getArtist()+","+i.getLike()+","+i.getGenre()+","+
                    i.getEncodedGenre()+","+i.getF0_1()+","+i.getF0_2()+i.getF0_3()+","+i.getF0_4()
            +","+i.getF0_5()+","+i.getF0_6()+","+i.getF0_7()+","+i.getF0_8()+"\n");
        }

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mydata.csv");
        response.setContentLength(csvData.length());

        PrintWriter writer = response.getWriter();
        writer.write(csvData.toString());

        return ResponseEntity.ok().build();
    }
}
