package com.example.cleancode.ddsp.controller;

import com.example.cleancode.ddsp.entity.InferenceQueue;
import com.example.cleancode.ddsp.entity.etc.InferenceRequest;
import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.ddsp.service.InferenceService;
import com.example.cleancode.ddsp.service.TrainService;
import com.example.cleancode.utils.CustomException.AwsUploadException;
import com.example.cleancode.utils.CustomException.DjangoRequestException;
import com.example.cleancode.utils.CustomException.NoAwsSongException;
import com.example.cleancode.utils.CustomException.NoPtrException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/ddsp")
@RequiredArgsConstructor
public class ddspController {
    private final InferenceService inferenceService;
    private final PtrDataRepository ptrDataRepository;
    private final TrainService trainService;
    @GetMapping("/sampleVoiceList")
    @ResponseBody
    public ResponseEntity<Object> samplePtrList(){
        Map<String,Object> body = new HashMap<>();
        List<PtrData> ptrDataList = ptrDataRepository.findAll();
        body.put("list",ptrDataList);
        return ResponseEntity.ok().body(ptrDataList);
    }
    @GetMapping("/generatedSongList")
    @ResponseBody
    public List<ResultSong> resultSongList(@RequestParam Long ptrId){
        return inferenceService.allResult(ptrId);
    }
    @PostMapping("/makesong")
    @ResponseBody
    public ResponseEntity<Object> ddspInferenceRequest(
            @RequestBody InferenceRequest inferenceRequest) {
        try{
            inferenceService.inferenceStart(
                    inferenceRequest.getTargetVoiceId(),
                    inferenceRequest.getTargetSongId());
        }catch (DjangoRequestException e){
            return ResponseEntity.status(e.getExceptionCode().getStatus())
                    .body(e.getExceptionCode().getMessage());
        }
        return ResponseEntity.ok().build();
    }
    @GetMapping("/progressList")
    @ResponseBody
    public ResponseEntity<Object> showStatus(@RequestParam Long ptrId, @RequestParam Long songId){
        String result = inferenceService.showStatus(ptrId,songId);
        Map<String,Object> body = new HashMap<>();
        body.put("status",result);
        return ResponseEntity.ok().body(body);
    }
    @PostMapping("/deleteSong")
    @ResponseBody
    public ResponseEntity<Object> ddspResultDelete(@RequestBody Integer generatedSongID){
        try{
            inferenceService.songDelete(generatedSongID);
        }catch (NoAwsSongException e){
            return ResponseEntity
                    .badRequest()
                    .body(e.getExceptionCode().getMessage());
        }
        return ResponseEntity.ok().build();
    }
    @PostMapping("/upload_ptr")
    @ResponseBody
    public ResponseEntity<Object> uploadPtr(
            @RequestPart("file") MultipartFile file,
            @RequestParam String name){
        try{
            trainService.ptrFileUplaod(file,name);
            return ResponseEntity.ok().
                    build();
        }catch (AwsUploadException e){
            return ResponseEntity.
                    status(e.getExceptionCode().getStatus())
                    .body(e.getExceptionCode().getMessage());
        }

    }
    @PostMapping("/delete_ptr")
    public ResponseEntity<Object> deletePtr(
            @RequestBody PtrData ptrData
    ){
        try{
            trainService.ptrFileDelete(ptrData);
            return ResponseEntity.ok().build();
        }catch (NoPtrException e){
            return ResponseEntity.status(e.getExceptionCode().getStatus())
                    .body(e.getExceptionCode().getMessage());
        }
    }
    @PostMapping("/update_ptr")
    @ResponseBody
    public ResponseEntity<Object> updatePtr(
            @RequestBody PtrData ptrData){
        try{
            Map<String,Object> body = new HashMap<>();
            body.put("status",trainService.ptrFileUpdate(ptrData));
            return ResponseEntity.ok().body(body);
        }catch (NoPtrException e){
            return ResponseEntity.status(e.getExceptionCode().getStatus())
                    .body(e.getExceptionCode().getMessage());
        }

    }
}
