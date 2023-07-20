package com.example.cleancode.api.service.oauth;

import com.example.cleancode.api.entity.*;
import com.example.cleancode.api.service.jwt.Jwt;
import com.example.cleancode.api.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthLoginService {
    private final UserRepository userRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final OAuthApiClient oAuthApiClient;
    private final JwtService jwtService;
    /**
     * authorization code 필요
     * @param
     */
    public Jwt login(KakaoLoginParams authorizationCode){

        OAuthResponse oAuthResponse = oAuthApiClient.requestAccessToken(authorizationCode); //토큰 받아옴
        InfoResponse Info = oAuthApiClient.requestUserInfo(oAuthResponse);
        Optional<User> userOptional = userRepository.findByEmail(Info.getEmail());
        if(userOptional.isEmpty()){
            User user = User.builder()
                    .email(Info.getEmail())
                    .name(Info.getNickname()).build();
            join(user);
            return jwtService.generate(user.getEmail());
        }
        return jwtService.generate(userOptional.get().getEmail());
    }

    public void join(User user){
        userRepository.save(user);
    }


}
