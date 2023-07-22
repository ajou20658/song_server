package com.example.cleancode.login.controlller;

import com.example.cleancode.login.service.AndroidLoginService;
import com.example.cleancode.login.service.oauth.AndroidRequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class AndroidLoginController {
    @Autowired
    private AndroidLoginService androidLoginService;

    @PostMapping("/login")
    public void login(@RequestBody AndroidRequestParam androidRequestParam){
        androidLoginService.join(androidRequestParam);
    }
}
