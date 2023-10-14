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
import com.example.cleancode.utils.CustomException.FormatException;
import com.example.cleancode.utils.CustomException.NoSongException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.beans.Encoder;
import java.io.*;
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

        if((!Objects.requireNonNull(type).contains("audio"))){
            throw new FormatException(ExceptionCode.FORMAT_ERROR);
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
    public void convertWavToMp3(MultipartFile multipartFile, String outputFilePath){
        try{
            File tempWavFile = File.createTempFile("temp", ".wav");
            FileOutputStream fos = new FileOutputStream(tempWavFile);
            fos.write(multipartFile.getBytes());
            fos.close();
            File mp3File = File.createTempFile("temp", ".mp3");
            String[] lameCommand = {
                    "lame",
                    tempWavFile.getAbsolutePath(),
                    mp3File.getAbsolutePath()
            };
            ProcessBuilder processBuilder = new ProcessBuilder(lameCommand);

            // 작업 디렉토리 설정 (필요에 따라 변경 가능)
            processBuilder.directory(new File("/path/to/lame/directory"));

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("MP3 conversion completed successfully.");
                byte[] mp3Bytes = getBytesFromFile(mp3File);
            } else {
                System.out.println("MP3 conversion failed with exit code: " + exitCode);
            }

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }
    public void preprocess(){

    }
    private static byte[] getBytesFromFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }

        fis.close();
        bos.close();

        return bos.toByteArray();
    }
}
