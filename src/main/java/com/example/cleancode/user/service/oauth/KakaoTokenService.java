package com.example.cleancode.user.service.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoTokenService {
    public static final String GRANT_TYPE = "authorization_code";
    @Value("${oauth.kakao.url.auth}")
    private String authUrl;
    @Value("${oauth.kakao.url.api}")
    private String apiUrl;
    @Value("${oauth.kakao.client-id}")
    private String clientId;
    @Value("${oauth.kakao.url.redirect-url}")
    private String redirectUrl;
    @Autowired
    private final WebClient webClient;
    public KakaoTokenResponse requestAccessToken(KakaoLoginParam params){
        String url = authUrl + "/oauth/token";
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("code",params.getAuthorizationCode());
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        body.add("redirect_uri",redirectUrl);
        log.info(body.toString());
        try{
            log.info(params.getAuthorizationCode());
            KakaoTokenResponse kakaoToken = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();
            return kakaoToken;
        }catch(WebClientResponseException ex){
            log.info("Kakao server err");
            return null;
        }
    }

    public KakaoInfoResponse requestUserInfo(String accessToken) {
        String url = apiUrl + "/v2/user/me";
        log.info("액세스 토큰입니다 : {}",accessToken);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("property_keys", "[\"kakao_account.email\", \"kakao_account.profile\"]");
        KakaoInfoResponse kakaoInfoResponse =  webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoInfoResponse.class).block();
        System.out.println("kakaoInfoResponseDto.toString() = " + kakaoInfoResponse.toString());
        System.out.println("kakaoInfoResponseDto.getEmail() = " + kakaoInfoResponse.getKakaoAccount().email);
        System.out.println("kakaoInfoResponseDto.getKakaoProfile() = " + kakaoInfoResponse.getKakaoAccount().profile.nickname);
        System.out.println("kakaoInfoResponseDto.getImage() = " + kakaoInfoResponse.getKakaoAccount().profile.thumbnail_image_url);
        return kakaoInfoResponse;
    }
    //유효성 검사
    public KakaoValidateResponse tokenInfo(String accessToken){
        String url = apiUrl + "/v1/user/access_token_info";
        try{
            KakaoValidateResponse kakaoValidateResponse = webClient.get()
                    .uri(url)
                    .header("Authorization","Bearer "+ accessToken)
                    .retrieve()
                    .bodyToMono(KakaoValidateResponse.class).block();
            System.out.println("kakaoValidateResponse.toString() = " + kakaoValidateResponse.toString());
            return kakaoValidateResponse;
        }catch (WebClientResponseException ex){
            return null;
        }
    }
    //재발급 요청
    public KakaoTokenResponse requestRenewToken(String refresh){
        String url = authUrl + "/oauth/token";
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("refresh_token",refresh);
        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", clientId);
        try{
            KakaoTokenResponse kakaoToken = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();
            return kakaoToken;
        }catch(WebClientResponseException ex){
            return null;
        }
    }
    //토큰 만료 요청
    public void tokenfire(String access,Long userId){
        String url = apiUrl+"/v1/user/logout";
        try {
//            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//            body.add("target_id_type","user_id");
//            body.add("target_id", String.valueOf(userId));
            URL url1 = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)url1.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization","Bearer "+access);
            int responseCode = conn.getResponseCode();
            log.info("kakao logout status= {}",responseCode);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = "";
            String line = "";
            while((line=br.readLine())!=null){
                result += line;
            }
            log.info(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
