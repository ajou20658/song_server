package com.example.cleancode.user.controller;

import com.example.cleancode.user.JpaRepository.MemberRepository;
import com.example.cleancode.user.dto.JwtDto;
import com.example.cleancode.user.dto.MemberDto;
import com.example.cleancode.user.entity.Member;
import com.example.cleancode.user.entity.UserPrinciple;
import com.example.cleancode.user.service.AndroidLoginService;
import com.example.cleancode.user.service.oauth.AndroidRequestParam;
import com.example.cleancode.utils.ApiResponseJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//http://3.34.194.47:8080/v1/login
@Slf4j
@RestController
@RequestMapping("/android")
public class AndroidLoginController {

    @Autowired
    private AndroidLoginService androidLoginService;
    @Autowired
    private MemberRepository memberRepository;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<String> login(@RequestBody AndroidRequestParam androidRequestParam){
        try{
            JwtDto jwtDto = androidLoginService.join(androidRequestParam);
            log.info("Token Issued : {}",jwtDto);
            ApiResponseJson apiResponseJson =  new ApiResponseJson(HttpStatus.OK,HttpStatus.OK.value(),jwtDto);
            log.info(apiResponseJson.toString());
            ObjectMapper mapper = new ObjectMapper();

            return new ResponseEntity<>(mapper.writeValueAsString(apiResponseJson),HttpStatus.OK);
        }catch (Exception ex){
            log.info("err");
            return null;
        }
    }
}
