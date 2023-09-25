package com.example.cleancode.song.service;

import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
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
@RequiredArgsConstructor
public class MelonCrawlService {
    private final SongRepository songRepository;

    private final Pattern pattern = Pattern.compile("(\\d+)(?=\\);)");
    private final Map<String,Long> dictionary = new HashMap<String,Long>(){
        {
            put("랩/힙합",0L);
            put("발라드",1L);
            put("댄스",2L);
            put("국내드라마",3L);
            put("인디음악",4L);
            put("록/메탈",5L);
            put("일렉트로니카",6L);
            put("포크/블루스",7L);
            put("R&B/Soul",8L);
            put("성인가요/트로트",9L);
            put("국내영화",10L);
            put("애니메이션/웹툰",11L);
            put("키즈",12L);
            put("만화",13L);
            put("J-POP",14L);
            put("재즈",15L);
            put("애시드/퓨전/팝",16L);
            put("POP",17L);
            put("국외영화",18L);
            put("클래식",19L);
            put("오페라/성악",20L);
            put("크로스오버",21L);
            put("게임",22L);
            put("창작동요",23L);
            put("국내뮤지컬",24L);
            put("-",25L);
            put("CCM",26L);
            put("국내CCM",27L);
            put("국악",28L);
            put("국악가요",29L);
            put("뉴에이지",30L);
            put("월드뮤직",31L);
            put("중국음악",32L);
            put("국외드라마",33L);
            put("보사노바",34L);
            put("포크",35L);
            put("자장가",36L);
            put("워십",37L);
        }
    };

//    @Scheduled(fixedRate = 21600000)
    public void collectMelonSong() throws Exception {
        List<SongDto> pList = new LinkedList<>();
        String url = "https://www.melon.com/chart/index.htm";

        Document doc = Jsoup.connect(url).get();

        Elements element = doc.select("div.service_list_song");
        for (Element songInfo : element.select("#lst50")) {
            SongDto songDto = top100CrawlParser(songInfo);
            pList.add(songDto);
        }

        Thread.sleep(3000);

        for (Element songInfo : element.select("#lst100")) {
            // 크롤링을 통해 데이터 저장하기
            SongDto songDto = top100CrawlParser(songInfo);
            pList.add(songDto);
        }

        String genreUrl = "https://www.melon.com/song/detail.htm?songId=";
        for(SongDto song: pList){
            String getGenreParam = String.valueOf(song.getId());
            Document genreDoc = Jsoup.connect(genreUrl+getGenreParam).get();
            SongDto songdto = genreImgUrlParser(genreDoc,song);
            songRepository.save(songdto.toChartEntity());
        }
    }
    public void artistCrawl() throws Exception{
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
                ex.printStackTrace((PrintStream) log);
            }

            assert doc != null;
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
    }
    public List<SongDto> search_artist(String artist, String mode) throws Exception{
        Long res =0L;
        List<SongDto> list = new LinkedList<>();
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
//        String all = "https://www.melon.com/search/song/index.htm?q="+artist+"&section=all&searchGnbYn=Y&kkoSpl=N&kkoDpType=#params%5Bq%5D="+artist+"&params%5Bsort%5D=hit&params%5Bsection%5D=all&params%5BsectionId%5D=&params%5BgenreDir%5D=&po=pageObj&startIndex=51";
        String m ="";
        switch(mode){
            case "0":
                m="all";
                break;
            case "1":
                m="artist";
                List<Song> result = songRepository.findByArtistContaining(artist);
                if(!result.isEmpty()){
                    return result.stream()
                            .map(Song::toChartDto)
                            .collect(Collectors.toList());
                }
                break;
            case "2":
                m="song";
                break;
            case "3":
                m="album";
                break;
        }
//        String EncodingArtist = URLEncoder.encode(artist, "UTF-8");
        String url = "https://www.melon.com/search/song/index.htm?q="+artist+"&section="+m+"&searchGnbYn=Y&kkoSpl=N&kkoDpType=%22%22#params%5Bq%5D="+artist+"&params%5Bsort%5D=hit&params%5Bsection%5D=artist&params%5BsectionId%5D=&params%5BgenreDir%5D=&po=pageObj&startIndex=";
        log.info(url);
        Connection connection = Jsoup.connect(url);
        try{
            Thread.sleep(3000);
            Document doc = connection.get();
            Elements rows = doc.select("#frm_defaultList > div > table > tbody>tr");
            for (Element row : rows) {
                try {
                    Elements tds = row.select("td");
                    Element td2 = tds.get(2);
                    String title = td2.select("div>div>a.fc_gray").first().text();
                    title = title.replace(","," ");
                    System.out.println("title = " + title);
                    //            #frm_defaultList > div > table > tbody > tr:nth-child(17) > td:nth-child(3) > div > div > a.fc_gray
                    Element td3 = tds.get(3);
                    String singer = td3.select("div>div>a").first().text();
                    if(!singer.contains(",")){
                        Element td4 = tds.get(4);
                        Element td5 = tds.get(5);

                        String likeId = td2.select("div>div>a.fc_gray").attr("href");
                        Matcher matcher = pattern.matcher(likeId);
                        String like = null;
                        if(matcher.find()){
                            like = matcher.group();
                            log.info(like);
                        }else {
                            like = "0";
                        }
                        String href = td4.select("div>div>a").attr("href");
                        String[] parse = parser(href);
                        SongDto songDto = SongDto.builder()
                                .title(title)
                                .artist(singer)
                                .id(Long.valueOf(parse[4]))
                                .likeId(Long.valueOf(like))
                                .build();
                        if(songRepository.findById(Long.valueOf(parse[4])).isEmpty()){
                            String genreUrl = "https://www.melon.com/song/detail.htm?songId=";
                            Document genreDoc = Jsoup.connect(genreUrl+parse[4]).get();
                            songDto = genreImgUrlParser(genreDoc,songDto);
                            songRepository.save(songDto.toChartEntity());
                        }
                        list.add(songDto);
                    }
                }catch (Exception ex){
                    ex.printStackTrace((PrintStream) log);
                }
            }
        }catch (Exception e){
            e.printStackTrace((PrintStream) log);
        }
        return list;
    }

    public JSONObject getLikeNum(List<Long> likeList)throws Exception{
        String url = "https://www.melon.com/commonlike/getSongLike.json?contsIds=";
        String param = likeList.stream()
                .map(Object::toString)
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
            return new JSONObject(jsonResponse);
        }else{
            throw new Exception("HTTP GET 요청 실패. 응답 코드 : "+responseCode);
        }
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
    private SongDto genreImgUrlParser(Document genreDoc,SongDto songDto){
        String genre = genreDoc.select("div.meta dd").eq(2).text();
        String imgUrl = genreDoc.select("#d_song_org > a > img").attr("src");
        List<String> genreArray;
        List<Long> encodedGenre = new ArrayList<>();
        if(genre.contains(",")){
            genreArray = List.of(genre.split(", "));
            for(String tmp:genreArray){
                encodedGenre.add(dictionary.get(tmp));
            }
        }else{
            genreArray = List.of(genre);
            System.out.println("genreArray = " + genreArray);
            encodedGenre.add(dictionary.get(genreArray.get(0)));
            System.out.println("encodedGenre = " + encodedGenre);
        }
        songDto.setImgUrl(imgUrl);
        songDto.setEncoded_genre(encodedGenre);
        songDto.setGenre(genreArray);
        return songDto;
    }
    private SongDto top100CrawlParser(Element songInfo){
        String songId = songInfo.attr("data-song-no");
        String like = null;
        if(songRepository.findById(Long.valueOf(songId)).isPresent()){
            return null;
        }
        String title = songInfo.select("div.ellipsis.rank01 a").text(); //제목
        String artist = songInfo.select("div.ellipsis.rank02 a").eq(0).text(); //가수
        String likeId = songInfo.select("div.ellipsis.rank01 a").attr("href");//좋아요 수 ID
        Matcher matcher = pattern.matcher(likeId);
        if(matcher.find()){
            like = matcher.group();
        }else {
            like = "0";
        }
        SongDto songDto = SongDto.builder()
                .likeId(Long.valueOf(like))
                .artist(artist)
                .title(title)
                .id(Long.valueOf(songId))
                .build();
        return songDto;
    }
    public void writeListToFile(Set<String> dataList,String filePath){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,true))) {
            for (String data : dataList) {
                writer.write(data);
                writer.newLine(); // 각 데이터를 새로운 줄에 작성
            }
        } catch (IOException e) {
            e.printStackTrace((PrintStream) log);
        }
    }
}