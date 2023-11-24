package com.example.cleancode.user.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.ddsp.entity.InferenceQueue;
import com.example.cleancode.ddsp.entity.PreprocessQueue;
import com.example.cleancode.ddsp.entity.etc.PreProcessRedisEntity;
import com.example.cleancode.song.entity.ProgressStatus;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.dto.UserSongDto;
import com.example.cleancode.user.dto.UserSongOutput;
import com.example.cleancode.user.entity.*;
import com.example.cleancode.utils.CustomException.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserSongRepository userSongRepository;
    private final SongRepository songRepository;
    private final Validator validator;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${spring.django-url}")
    private String djangoUrl;
    private final WebClient webClient;
    private final InferenceQueue inferenceQueue;
    private final PreprocessQueue preprocessQueue;
    public UserDto findMember(Long userId){
        User member = validator.userValidator(userId);
        return member.toMemberDto();
    }

    @Transactional
    public ProgressStatus userUploadCheck(Long userId,Long songId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        return userSong.getStatus();
    }

    //folder 이름 형식 : user/userId_songId
    @Transactional
    public boolean userFileUpload(MultipartFile multipartFile,Long userId,Long songId){ //동일한 곡에 대해 여러값이 들어가는 오류
        User user = validator.userValidator(userId);
        Song song = validator.songValidator(songId);

        Optional<UserSong> userSongOptional = userSongRepository.findBySongIdAndUserId(songId,userId);

        log.info("File type : {}",multipartFile.getContentType());
        String type = multipartFile.getContentType();

        if(!Objects.requireNonNull(type).contains("mpeg")){
            throw new FormatException(ExceptionCode.FORMAT_ERROR);
        }
        String filename="";
        //존재 여부에 따라 다르게
        filename = userSongOptional.map(userSong -> "origin/" + userSong.getOriginUrl().split("/")[1]).orElseGet(() -> "origin/" + UUID.randomUUID());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        UserSong userSong = UserSong.builder()
                .vocalUrl("")
                .originUrl(filename)
                .user(user)
                .song(song)
                .status(ProgressStatus.COMPLETE)
                .build();
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        if(userSongOptional.isPresent()){
            return true;
        }
        userSongRepository.save(userSong);
        return true;
    }


    @Transactional
    public boolean preprocessStart(Long songId, Long userId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        //---------전처리 시작 UserSong Status변경
        String uuid = userSong.getOriginUrl().split("/")[1];
        PreProcessRedisEntity preProcessRedisEntity = PreProcessRedisEntity.builder()
                .uuid(uuid)
                .songId(String.valueOf(songId))
                .userId(String.valueOf(userId))
                .build();
        try {
            //전처리 요청

            MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
            body.add("fileKey",userSong.getOriginUrl());
            body.add("isUser","true");
            body.add("uuid",uuid);

            log.info(body.toString());

            String url = "http://"+djangoUrl + "/songssam/splitter/";
            Mono<Dataframe2Json> response = webClient.post()
                    .uri(url)
                    .body(BodyInserters.fromFormData(body))
                    .retrieve()
                    .onStatus(
                            httpStatusCode ->
                                    httpStatusCode.is4xxClientError()||
                                    httpStatusCode.is5xxServerError(),
                            clientResponse -> Mono.error(new DjangoRequestException(ExceptionCode.WEB_CLIENT_ERROR))
                    )
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofMinutes(5))
                    .handle((JsonNode, sink) -> {
                        try{
                            String message = JsonNode.get("message").asText();
                            ObjectMapper objectMapper = new ObjectMapper();
                            Dataframe2Json[] result = objectMapper.readValue(message,Dataframe2Json[].class);
                            sink.next(result[0]);
                        }catch (JsonProcessingException e){
                            log.error("파싱 에러");
                            sink.error(new DjangoRequestException(ExceptionCode.WEB_CLIENT_ERROR));
                        }
                    });
            preprocessQueue.pushInProgress(preProcessRedisEntity);
            djangoRequest(response, userSong,uuid);
            log.info("preprocess(split,f0_extract) 요청 성공");
        } catch (Exception ex){
            userSong.changeStatus(ProgressStatus.ERROR);
            preprocessQueue.changeStatus(preProcessRedisEntity,ProgressStatus.ERROR);
            userSongRepository.save(userSong);
            return false;
        }
        return true;
    }
    @Async
    @Transactional
    public void djangoRequest(Mono<Dataframe2Json> response,UserSong userSong,String uuid){
        PreProcessRedisEntity preProcessRedisEntity = PreProcessRedisEntity.builder()
                .songId(String.valueOf(userSong.getSong().getId()))
                .userId(String.valueOf(userSong.getUser().getId()))
                .build();
        response.subscribe(res -> {
            log.info("status message = {}",res.getF0_1());
            List<Integer> spectr = json2List(res);
            UserDto userDto = userSong.getUser().toMemberDto();
            UserSongDto userSongDto = userSong.toUserSongDto();
            userSongDto.setVocalUrl("vocal/"+uuid);
            userSongDto.setSpectr(spectr);
            userSongDto.setStatus(ProgressStatus.COMPLETE);
            userSongRepository.save(userSongDto.toUserSong());
            userRepository.save(userDto.makeMember());
            preprocessQueue.changeStatus(preProcessRedisEntity,ProgressStatus.COMPLETE);
        });
    }
    private List<Integer> json2List(Dataframe2Json rawJson){
        List<Integer> result = new ArrayList<>();
        result.add(rawJson.getF0_1());
        result.add(rawJson.getF0_2());
        result.add(rawJson.getF0_3());
        result.add(rawJson.getF0_4());
        result.add(rawJson.getF0_5());
        result.add(rawJson.getF0_6());
        result.add(rawJson.getF0_7());
        result.add(rawJson.getF0_8());
        log.info(result.toString());
        return result;
    }
    @Transactional
    public boolean reIssueRecommandList(List<Long> song,Long userId){
        User user = validator.userValidator(userId);
        if(!changeSelectList(song,user)){
            return false;
        }
        List<Long> recommendList = requestRecommandSongId(userId);
        //사용자가 선택한 곡도 추가
        recommendList.addAll(song);

        UserDto userDto = user.toMemberDto();
        userDto.setRecommandSongIds(recommendList);
        userRepository.save(userDto.makeMember());
        return true;
    }
    @Transactional
    public boolean changeSelectList(List<Long> song, User user){
        UserDto userDto = user.toMemberDto();
        userDto.setSelected(song);
        userRepository.save(userDto.makeMember());
        return true;
    }
    public List<UserSongOutput> readUserSongList(Long userId){
        List<UserSong> list = userSongRepository.findByUserId(userId);
        List<UserSongOutput> result = new ArrayList<>();

        for(UserSong song1: list){
            result.add(song1.toUserSongDto().outputFormat());
        }
        return result;
    }
    @Transactional
    public void userFileDelete(Long songId,Long userId){
        UserSong userSong = validator.userSongValidator(songId,userId);
        if(!userSong.getVocalUrl().isEmpty()){    //분리후 압축 된 것 저장
            log.info("SongId,UserId AwsUrl제거 : {}", userSong.getVocalUrl());
            amazonS3.deleteObject(bucket,userSong.getVocalUrl());
        }
        if(!userSong.getOriginUrl().isEmpty()){ //원본 파일
            log.info("SongId,UserId OriginUrl제거 : {}", userSong.getOriginUrl());
            amazonS3.deleteObject(bucket,userSong.getOriginUrl());
        }
        userSongRepository.delete(userSong);
    }
    public List<Song> userLikeSongList(Long userId){
        User user = validator.userValidator(userId);
        List<Long> songNumList = user.getSelected();
        List<Song> songList = new ArrayList<>();
        for(Long i:songNumList){
            songList.add(validator.songValidator(i));
        }
        return songList;
    }

    public List<Song> userLikeSongWithRecommand(Long userId){
        User user = validator.userValidator(userId);
        List<Long> songRecommandList = user.getRecommandSongIds();
        List<Song> songList = new ArrayList<>();
        Collections.shuffle(songRecommandList);
        if(songRecommandList.size()<4){
            for(Long i:songRecommandList){
                songList.add(validator.songValidator(i));
            }
            return songList;
        }
        for(Long i:songRecommandList.subList(0,4)){
            songList.add(validator.songValidator(i));
        }
        return songList;
    }
    private List<Long> requestRecommandSongId(Long userId){
        List<Song> songList = userLikeSongList(userId);
        //여기서 장르 추출
        GenreCountFrame user_genre = genreCount(songList);
        Spectr2DataFrame user_f0 = f02Df(userId);
        log.info(user_genre.toString());
        log.info(user_f0.toString());
        RecommandRequestDataFrame user_info_json =
                new RecommandRequestDataFrame(user_f0,user_genre);
        ObjectMapper objectMapper =new ObjectMapper();
        String json="";
        try {
             json = objectMapper.writeValueAsString(Collections.singletonMap("user_info_json",user_info_json));
             log.info(json);
        }catch (Exception e){
            log.error("json 변환 오륲");
        }

        return webClient.post()
                .uri("http://localhost:8000/SongRecommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(json))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody->{
                    try{
                        JsonNode jsonNode = objectMapper.readTree(responseBody);
                        JsonNode songIdNode = jsonNode.get("song_id");
                        List<Long> ids = new ArrayList<>();
                        if(songIdNode!=null&&songIdNode.isArray()){
                            for(JsonNode iNode:songIdNode){
                                ids.add(iNode.asLong());
                            }
                        }
                        return Mono.just(ids);
                    }catch (Exception e){
                        log.error(e.getMessage());
                        return Mono.error(e);
                    }
                })
                .block();
    }
    private GenreCountFrame genreCount(List<Song> songList){
        GenreCountFrame genreCountFrame = GenreCountFrame.builder().build();
        for (Song i : songList){
            List<String> genre = i.genre;
            for(String g:genre){
                switch (g){
                    case "J-POP":
                        genreCountFrame.setA(genreCountFrame.getA()+1);
                        break;
                    case "POP":
                        genreCountFrame.setB(genreCountFrame.getB()+1);
                        break;
                    case "R&B/Soul":
                        genreCountFrame.setC(genreCountFrame.getC()+1);
                        break;
                    case "국내드라마":
                        genreCountFrame.setD(genreCountFrame.getD()+1);
                        break;
                    case "댄스":
                        genreCountFrame.setE(genreCountFrame.getE()+1);
                        break;
                    case "랩/힙합":
                        genreCountFrame.setF(genreCountFrame.getF()+1);
                        break;
                    case "록/메탈":
                        genreCountFrame.setG(genreCountFrame.getG()+1);
                        break;
                    case "발라드":
                        genreCountFrame.setH(genreCountFrame.getH()+1);
                        break;
                    case "성인가요/트로트":
                        genreCountFrame.setI(genreCountFrame.getI()+1);
                        break;
                    case "애니메이션/웹툰":
                        genreCountFrame.setJ(genreCountFrame.getJ()+1);
                        break;
                    case "인디음악":
                        genreCountFrame.setK(genreCountFrame.getK()+1);
                        break;
                    case "일렉트로니카":
                        genreCountFrame.setL(genreCountFrame.getL()+1);
                        break;
                    case "포크/블루스":
                        genreCountFrame.setM(genreCountFrame.getM()+1);
                        break;
                    default:
                        break;
                }
            }
        }
        return genreCountFrame;
    }
    private Spectr2DataFrame f02Df(Long userId){
        User user  = validator.userValidator(userId);
        List<Integer> spectrum = user.getSpectr();
        if(spectrum.isEmpty()){
            throw new BadRequestException(ExceptionCode.NO_F0_DATA);
        }
        return new Spectr2DataFrame(
                spectrum.get(0),
                spectrum.get(1),
                spectrum.get(2),
                spectrum.get(3),
                spectrum.get(4),
                spectrum.get(5),
                spectrum.get(6),
                spectrum.get(7)
                );
    }
}
