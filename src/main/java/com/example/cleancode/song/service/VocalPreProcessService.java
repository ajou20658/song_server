package com.example.cleancode.song.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VocalPreProcessService {
    public MultipartFile split(MultipartFile file){
        String url = "localhost:8000/seperate_audio?input_path=&output_dir=";

        return null;
    }


}
