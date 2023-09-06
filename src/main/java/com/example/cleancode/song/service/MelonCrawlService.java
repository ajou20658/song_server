package com.example.cleancode.song.service;

import com.example.cleancode.song.dto.ChartDTO;
import com.example.cleancode.song.dto.SearchDto;
import com.example.cleancode.song.entity.Album;
import com.example.cleancode.song.entity.Chart;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.AlbumRepository;
import com.example.cleancode.song.repository.ChartRepository;
import com.example.cleancode.song.repository.SongRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MelonCrawlService {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private ChartRepository chartRepository;

    /**
     * do-crawl에서 사용
     * 실행시 이전에 존재하는 데이터는 삭제
     * @return mongodb에 저장됨
     * @throws Exception
     */
    public Long collectMelonSong() throws Exception {
        chartRepository.deleteAll();
        Long res = 0l;
        List<ChartDTO> pList = new LinkedList<>();
        String url = "https://www.melon.com/chart/index.htm";

        Document doc = Jsoup.connect(url).get();

        // <div class="service_list_song"> 이 태그 내에 있는 HTML 소스만 element에 저장된다.
        Elements element = doc.select("div.service_list_song");
        log.info(element.toString());
        for (Element songInfo : element.select("#lst50")) {
            log.info(songInfo.toString());
            // 크롤링을 통해 데이터 저장하기
            String songId = songInfo.attr("data-song-no");
            String albumId = songInfo.select("div.ellipsis.rank03 a").attr("href").substring(37,45);
            String title = songInfo.select("div.ellipsis.rank01 a").text(); //노래
            String artist = songInfo.select("div.ellipsis.rank02 a").eq(0).text(); //가수
            String imgUrl = songInfo.select(".image_typeAll img").attr("src");
            log.info("song : {}", title);
            log.info("singer : {}", artist);

            if ((title.length() > 0) && (artist.length() > 0)) {

                ChartDTO chartDTO = ChartDTO.builder()
                        .albumId(albumId)
                        .artist(artist)
                        .title(title)
                        .songId(songId)
                        .imgUrl(imgUrl)
                        .build();
                //한 번에 여러 개의 데이터를 MongoDB에 저장할 List 형태의 데이터 저장하기
                pList.add(chartDTO);
            }
        }
        log.info(pList.toString());
        //MongoDB에 데이터저장하기
        //res = iMelonMapper.insertSong(pList, cloNm);
        String genreUrl = "https://www.melon.com/song/detail.htm?songId=";
        for(ChartDTO chart: pList){
            String getGenreParam = chart.getSongId();
            Document genreDoc = Jsoup.connect(genreUrl+getGenreParam).get();
            String genre = genreDoc.select("div.meta dd").eq(2).text();
            if(genre.contains(",")){
                List<String> genreArray = List.of(genre.split(", "));
                chart.setGenre(genreArray);
            }
            chartRepository.save(chart.toChartEntity());
            res+=1;
            log.info("crawl count: "+res);
        }
        log.info(this.getClass().getName() + ".collectionMelonSong End");

        return res;
    }
    /**
     * 100~800번
     * @param
     * @return
     */
    public boolean artistCrawl() throws Exception{
        Set<String> artistList = new HashSet<>();
        List<String> mode = new ArrayList<String>(Arrays.asList("100","200","300","400","500","600","700","800"));
        for(String i :mode){
            String url = "https://www.melon.com/chart/month/index.htm?classCd=GN0"+i;
            Connection connect = Jsoup.connect(url);
            Document doc=null;
            try{
                Thread.sleep(10000);
                doc = connect.get();
            }catch (InterruptedException ex){
                ex.printStackTrace();
            }

            Elements elements = doc.select("div.service_list_song");
            System.out.println("elements = " + elements);
            for(Element element : elements.select("#lst50")){
                String artist = element.select("div.ellipsis.rank02 a").eq(0).text(); //가수
                log.info(artist);
                if(!artist.contains(",")) {
                    artistList.add(artist);
                }
            }
            for(Element element : elements.select("#lst100")){
                String artist = element.select("div.ellipsis.rank02 a").eq(0).text(); //가수
                log.info(artist);
                if(!artist.contains(",")) {
                    artistList.add(artist);
                }
            }
            log.info(artistList.toString());
        }
        writeListToFile(artistList,"C:\\Users\\kwy\\Documents\\2023하계\\cleancode\\src\\main\\resources\\static\\artist.txt");
        return true;
    }
    /**
     * search에서 사용
     * @param artist 검색할 내용
     * @param mode 검색 방법
     * @return json 형태로 반환
     * @throws Exception
     */
    public List<SearchDto> search_artist(String artist, String mode) throws Exception{
        Long res =0l;
        List<SearchDto> list = new LinkedList<>();
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
//        String all = "https://www.melon.com/search/song/index.htm?q="+artist+"&section=all&searchGnbYn=Y&kkoSpl=N&kkoDpType=#params%5Bq%5D="+artist+"&params%5Bsort%5D=hit&params%5Bsection%5D=all&params%5BsectionId%5D=&params%5BgenreDir%5D=&po=pageObj&startIndex=51";
        String m ="";
        switch(mode){
            case "0":
                m="all";
                break;
            case "1":
                m="artist";
                break;
            case "2":
                m="song";
                break;
            case "3":
                m="album";
                break;
        }
        String url = "https://www.melon.com/search/song/index.htm?q="+artist+"&section="+m+"&searchGnbYn=Y&kkoSpl=N&kkoDpType=%22%22#params%5Bq%5D="+artist+"&params%5Bsort%5D=hit&params%5Bsection%5D=artist&params%5BsectionId%5D=&params%5BgenreDir%5D=&po=pageObj&startIndex=";
        log.info(url);
        Connection connection = Jsoup.connect(url);
        try{
            Thread.sleep(5000);
            Document doc = connection.get();
            Elements rows = doc.select("#frm_defaultList > div > table > tbody>tr");
            for (Element row : rows) {
                try {
                    Elements tds = row.select("td");
    //                Element td1 = tds.get(1);
                    Element td2 = tds.get(2);
                    String title = td2.select("div>div>a.fc_gray").first().text();
                    //            #frm_defaultList > div > table > tbody > tr:nth-child(17) > td:nth-child(3) > div > div > a.fc_gray
                    Element td3 = tds.get(3);
                    String singer = td3.select("div>div>a").first().text();
                    Element td4 = tds.get(4);
                    String album = td4.text();
                    Element td5 = tds.get(5);

                    String likeId = td2.select("div>div>a.fc_gray").attr("href").toString();
                    Matcher matcher = pattern.matcher(likeId);
                    String like = null;
                    if(matcher.find()){
                        like = matcher.group();
                        log.info(like);
                    }
                    String href = td4.select("div>div>a").attr("href");
                    String onClickValue4 = td3.select("a").attr("onclick");
    //                log.info(onClickValue4);
                    String[] parse = parser(href);

                    list.add(SearchDto.builder()
                            .title(title)
                            .artist(singer)
                            .albumId(parse[5])
                            .albumTitle(album)
                            .songId(parse[4])
                            .likeId(like)
                            .build());
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
    public List<Chart> getAllChart() throws Exception{
        return chartRepository.findAll();
    }

    public Long saveSong(Song song) throws Exception{
        boolean isExits = songRepository.existsById(song.getSongId());
        songRepository.save(song);
        return 1L; //O.K.
    }

    public Long getNumChart() throws Exception{
        return chartRepository.count();
    }

    public List<Album> getAllAlbum() throws Exception {
        return albumRepository.findAll();
    }

    public JSONObject getLikeNum(List<String> likeList)throws Exception{
        String url = "https://www.melon.com/commonlike/getSongLike.json?contsIds=";
        String param = likeList.stream()
                .collect(Collectors.joining(","));
        String request = url+param;
        URL obj = new URL(request);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while((inputLine=in.readLine())!=null){
                response.append(inputLine);
            }
            in.close();

            String jsonResponse = response.toString();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject;
        }else{
            throw new Exception("HTTP GET 요청 실패. 응답 코드 : "+responseCode);
        }
    }
    public List<Song> getSongByAlbum(Long albumId) throws Exception {
        return null;
    }
    private String[] parser(String href){
        Pattern pattern = Pattern.compile("'([^']*)'"); // This pattern captures the text inside single quotes
        Matcher matcher = pattern.matcher(href);

        // Find the first three matches and extract the values
        String[] values = new String[9];
        int index = 0;
        while (matcher.find()) {
            values[index++] = matcher.group(1);
        }
        return values;
    }
    public void writeListToFile(Set<String> dataList,String filePath){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,true))) {
            for (String data : dataList) {
                writer.write(data);
                writer.newLine(); // 각 데이터를 새로운 줄에 작성
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}