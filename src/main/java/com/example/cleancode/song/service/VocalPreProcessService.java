package com.example.cleancode.song.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.song.dto.SongDto;
import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.utils.CustomException.ExceptionCode;
import com.example.cleancode.utils.CustomException.NoSongException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Encoder;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VocalPreProcessService {
    private final SongRepository songRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    public boolean songUpload(MultipartFile multipartFile, Long songId){
        String type = multipartFile.getContentType();
        if(!type.contains("audio")){
            return false;
        }
        UUID uuid = UUID.randomUUID();
        String filename = "song/"+uuid;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        Optional<Song> songOptional = songRepository.findById(songId);
        if(songOptional.isEmpty()){
            throw new NoSongException(ExceptionCode.SONG_INVALID);
        }
        SongDto songDto = songOptional.get().toSongDto();
        songDto.setOriginUrl(filename);
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            songRepository.save(songDto.toSongEntity());
        }catch (IOException | SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
//    public void convertWavToMp3(MultipartFile multipartFile){
//        Encoder encoder = new Encoder();
//        MultimediaInf
//    }
    public void preprocess(){

    }
}
