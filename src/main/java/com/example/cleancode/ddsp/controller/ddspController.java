package com.example.cleancode.ddsp.controller;

import com.example.cleancode.ddsp.entity.InferenceRequest;
import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.ddsp.service.InferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ddsp")
@RequiredArgsConstructor
public class ddspController {
    private final InferenceService inferenceService;
    private final PtrDataRepository ptrDataRepository;
    @GetMapping("/sampleVoiceList")
    public ResponseEntity<Object> samplePtrList(){
        Map<String,Object> body = new HashMap<>();
        List<PtrData> ptrDataList = ptrDataRepository.findAll();
        body.put("list",ptrDataList);
        return ResponseEntity.ok().body(ptrDataList);
    }
    @GetMapping("/generatedSongList")
    public List<ResultSong> resultSongList(@RequestParam Long ptrId){
        return inferenceService.allResult(ptrId);
    }
    @PostMapping("/makesong")
    public ResponseEntity<Object> ddspInferenceRequest(
            @RequestBody InferenceRequest inferenceRequest){
        Integer generatedSongId = inferenceService.inferenceStart(
                inferenceRequest.getTargetVoiceId(),
                inferenceRequest.getTargetSongId());
        Map<String,Object> body = new HashMap<>();
        body.put("generatedId",generatedSongId);
        return ResponseEntity.ok().body(body);
    }
    @PostMapping("/deleteSong")
    public ResponseEntity<Object> ddspResultDelete(@RequestBody Integer deleteId){
        inferenceService.songDelete(deleteId);
        return ResponseEntity.ok().build();
    }
}
