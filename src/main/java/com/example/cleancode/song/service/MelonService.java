package com.example.cleancode.song.service;

import com.example.cleancode.song.dto.ChartDTO;
import com.example.cleancode.song.dto.SearchDto;
import com.example.cleancode.song.entity.Album;
import com.example.cleancode.song.entity.Chart;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.AlbumRepository;
import com.example.cleancode.song.repository.ChartRepository;
import com.example.cleancode.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MelonService {
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private ChartRepository chartRepository;

    public ChartDTO findById(String Id){
        Optional<Chart> opt = chartRepository.findById(Id);
        if(opt.isEmpty()){
            return null;
        }
        return ChartDTO.builder()
                .albumId(opt.get().albumId)
                .artist(opt.get().artist)
                .genre(opt.get().genre)
                .imgUrl(opt.get().imgUrl)
                .songId(opt.get().songId)
                .title(opt.get().title)
                .build();
    }
    public List<ChartDTO> findByArtist(String Artist){
        List<Chart> opt = chartRepository.findByArtist(Artist);
        if(opt.isEmpty()){
            return null;
        }
        List<ChartDTO> chartDTOList = new ArrayList<>();
        for (Chart chart : opt) {
            ChartDTO chartDTO = ChartDTO.builder()
                    .albumId(chart.albumId)
                    .artist(chart.artist)
                    .genre(chart.genre)
                    .imgUrl(chart.imgUrl)
                    .songId(chart.songId)
                    .title(chart.title)
                    .build();
            chartDTOList.add(chartDTO);
        }
        return chartDTOList;
    }
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
            chart.setGenre(genre);
            chartRepository.save(DtoToEntity(chart));
            res+=1;
            log.info("crawl count: "+res);
        }
        log.debug(this.getClass().getName() + ".collectionMelonSong End");

        return res;
    }
    public List<SearchDto> search_artist(String artist) throws Exception{
        Long res =0l;
        List<SearchDto> list = new LinkedList<>();
        String url = "https://www.melon.com/search/song/index.htm?q="+artist+"&section=artist&searchGnbYn=Y&kkoSpl=N&kkoDpType=%22%22#params%5Bq%5D="+artist+"&params%5Bsort%5D=hit&params%5Bsection%5D=artist&params%5BsectionId%5D=&params%5BgenreDir%5D=&po=pageObj&startIndex=";
        Document doc = Jsoup.connect(url).get();
        Elements element = doc.select("div.service_list_song");
        Elements navigate = doc.select("#pageObjNavgation > div > span");
        System.out.println("doc = " + doc);
        System.out.println("element = " + element);
        System.out.println("navigate = " + navigate);


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


    public List<Song> getSongByAlbum(Long albumId) throws Exception {
        return null;
    }

    public Chart DtoToEntity (ChartDTO chartDTO){
        Chart chart = new Chart(
                chartDTO.getSongId(),
                chartDTO.getTitle(),
                chartDTO.getImgUrl(),
                chartDTO.getArtist(),
                chartDTO.getAlbumId(),
                chartDTO.getGenre()
        );
        return chart;
    }
    public ChartDTO EntityToDto (Chart chart){
        return ChartDTO.builder()
                .title(chart.title)
                .songId(chart.songId)
                .imgUrl(chart.imgUrl)
                .genre(chart.genre)
                .artist(chart.artist)
                .albumId(chart.albumId)
                .build();
    }

}