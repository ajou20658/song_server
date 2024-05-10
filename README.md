<img src="https://capsule-render.vercel.app/api?type=waving&color=037bfc&height=150&section=header" />

## 파란학기 SongSSam 프로젝트의 백엔드 서버
2023-2학기 파란학기로 진행한 프로젝트입니다.
## 목차
[API 명세](#api-명세)

[Spring 서버 구조](#spring-서버)

[Spring-Django 요청 흐름](#spring과-django의-요청-흐름도)

[파란학기 결과 템플릿](#파란학기-결과-템플릿)
## API 명세
### API 명세 목차
* [AuthController](#authcontroller)
  * [로그인](#로그인)
  * [jwt 갱신](#jwt-갱신)
* [usercontroller](#usercontroller)
  * [프로필 조회](#프로필-조회)
  * [사용자 선호곡 업데이트](#사용자-선호곡-업데이트)
  * [사용자 선호곡 조회](#사용자-선호곡-조회)
  * [사용자 추천곡 조회](#사용자-추천곡-조회)
  * [사용자 녹음 파일 업로드](#사용자-녹음-파일-업로드)
  * [업로드된 녹음 파일 조회](#업로드된-녹음-파일-조회)
  * [업로드된 녹음 파일 삭제](#업로드된-녹음-파일-삭제)
* [songcontroller](#songcontroller)
  * [노래 목록 조회](#노래-목록-조회)
  * [노래 검색](#노래-검색)
  * [음원 업로드](#음원-업로드)
  * [음원 삭제](#음원-삭제)
  * [전처리가 완료된 음원 랜덤 조회](#전처리가-완료된-음원-랜덤-조회)
  * [음원 파일 듣기](#음원-파일-듣기)
  * [전처리 요청](#전처리-요청)
  * [음원 메타데이터 csv로 다운로드](#음원-메타데이터-csv로-다운로드)
* [DDSPController](#ddspcontroller)
  * [ptr파일 조회](#ptr-파일-조회)
  * [생성된 AI 커버곡 조회](#생성된-ai-커버곡-조회)
  * [AI 커버곡 생성 요청](#ai-커버곡-생성-요청)
  * [AI 커버곡 삭제](#ai-커버곡-삭제)

## AuthController
### 로그인
* URL: `/auth/login`
* Method: `POST`
* RequestBody: `"authorizationCode":{authorizationCode}`
* ResponseBody:
  ```
  {
    "HttpStatus":"200",
    "response:":{
                  "accessToken":{accessToken},
                  "refreshToken":{refreshToken}
                }
  }
  ```
### jwt 갱신
* URL: `/auth/jwtUpdate`
* Method: `POST`
* RequestBody:
  ```
  {
    "accessToken":{accessToken},
    "refreshToken":{refreshToken}
  }
  ```
* ResponseBody:
  ```
  {
    "accessToken":{accessToken},
    "refreshToken":{refreshToken}
  }
  ```
## UserController
### 프로필 조회
* URL: `/member/info`
* Method: `GET`
* RequestHeader: `"Authorization":"Bearer {AccessToken}"`
* Response:
  ```
  {
    "id":"회원ID",
    "nickname":"oauth2에 등록된 이름",
    "profileUrl":"oauth2에 등록된 프로필 경로",
    "role":"회원의 권한",
    "spectr":["f0",...,"f7"] 회원의 전처리된 음성데이터를 기반으로 클러스터링을 통해 얻은 대표 음역대,
    "selected":[songId1,songId2,songId3,...] 회원이 선택한 선호 음악,
    "recommandSongIds":[songId1,songId2,songId3,...] 회원의 정보와 외부 정보를 기반으로 한 추천알고리즘의 결과
  }
  ```
### 사용자 선호곡 업데이트
* URL: `/member/user_list`
* Method: `POST`
* RequestBody:
  ```
    "songList":[songId1,songId2,...]
  ```
* Response: (HTTP Status 200)
### 사용자 선호곡 조회
* URL: `/member/user_list`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":"곡Id:long",
      "title":"곡제목:string",
      "imgUrl":"대표 이미지:string",
      "artist":"가수:string",
      "isTop":"top100여부:boolean",
      "spectr":[
                "f0":"제일 낮은 음역대:int",
                "f1",..."f7",
                "f8":"제일 높은 음역대:int"
              ],
      "vocalUrl":"음원에서 분리된 보컬 파일 경로",
      "instUrl":"음원에서 분리된 MR 파일 경로"
      "originUrl":"30초의 미리듣기를 위한 짧은 원곡 파일 경로",
      "genre":[
                "장르1",
                "장르2",...
              ]
      "status":"음원 파일 처리 여부"
    },
  ]
  ```

### 사용자 추천곡 조회
* URL: `/member/user_recommand_list`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":"곡Id:long",
      "title":"곡제목:string",
      "imgUrl":"대표 이미지:string",
      "artist":"가수:string",
      "isTop":"top100여부:boolean",
      "spectr":[
                "f0":"제일 낮은 음역대:int",
                "f1",..."f7",
                "f8":"제일 높은 음역대:int"
              ],
      "vocalUrl":"음원에서 분리된 보컬 파일 경로",
      "instUrl":"음원에서 분리된 MR 파일 경로"
      "originUrl":"30초의 미리듣기를 위한 짧은 원곡 파일 경로",
      "genre":[
                "장르1",
                "장르2",...
              ]
      "status":"음원 파일 처리 여부"
    },
  ]
  ```
### 사용자 녹음 파일 업로드
* URL: `/member/upload?songId={songId}`
* Method: `POST`
* RequestHeader: `Content-Type: multipart/form-data`
* Response: (HTTP Status 200)
  ```
    "response":"songId"
  ```

### 업로드된 녹음 파일 조회
* URL: `/member/vocal_list`
* Method: `GET`
* Response:
  ```
  {
    "songId":"노래Id",
    "vocalUrl":"분리된 보컬 파일 경로",
    "originUrl":"업로드된 원본 파일 경로",
    "spectr":[f0,f1,...,f7] 해당 곡의 음역대,
    "createdAt":"생성된 시간",
    "userId":"회원ID",
    "status":"처리 진행 상태"
  }
  ```
### 업로드된 녹음 파일 삭제
* URL: `/member/deleteVocalFile"
* Method: `POST`
* Respoonse: (HTTP Status 200)
* 

## SongController
### 노래 목록 조회
* URL: `/song/chartjson`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":"곡Id:long",
      "title":"곡제목:string",
      "imgUrl":"대표 이미지:string",
      "artist":"가수:string",
      "isTop":"top100여부:boolean",
      "spectr":[
                "f0":"제일 낮은 음역대:int",
                "f1",..."f7",
                "f8":"제일 높은 음역대:int"
              ],
      "vocalUrl":"음원에서 분리된 보컬 파일 경로",
      "instUrl":"음원에서 분리된 MR 파일 경로"
      "originUrl":"30초의 미리듣기를 위한 짧은 원곡 파일 경로",
      "genre":[
                "장르1",
                "장르2",...
              ]
      "status":"음원 파일 처리 여부"
    },
  ]
  ```
### 노래 검색
* URL: `/song/search?target={target}&mode={mode}`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":"곡Id:long",
      "title":"곡제목:string",
      "imgUrl":"대표 이미지:string",
      "artist":"가수:string",
      "instUrl":"음원에서 분리된 MR 파일 경로"
      "originUrl":"30초의 미리듣기를 위한 짧은 원곡 파일 경로",
      "genre":[
                "장르1",
                "장르2",...
              ]
      "status":"음원 파일 처리 여부"
    },
  ]
  ```

### 음원 업로드
* URL: `/song/upload?songId={songId}`
* Method: `POST`
* RequestHeader: `Content-Type: multipart/form-data`
* Response: (HTTP Status 200)
  ```
    "response":"songId"
  ```

### 음원 삭제
* URL: `/song/removeFile/{songId}`
* Method: `DELETE`
* Response: (HTTP Status 200)

### 전처리가 완료된 음원 랜덤 조회
* URL: `/song/completed_random_list`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":"곡Id:long",
      "title":"곡제목:string",
      "imgUrl":"대표 이미지:string",
      "artist":"가수:string",
      "isTop":"top100여부:boolean",
      "spectr":[
                "f0":"제일 낮은 음역대:int",
                "f1",..."f7",
                "f8":"제일 높은 음역대:int"
              ],
      "vocalUrl":"음원에서 분리된 보컬 파일 경로",
      "instUrl":"음원에서 분리된 MR 파일 경로"
      "originUrl":"30초의 미리듣기를 위한 짧은 원곡 파일 경로",
      "genre":[
                "장르1",
                "장르2",...
              ]
      "status":"음원 파일 처리 여부"
    },
  ]
  ```
### 음원 파일 듣기
* URL: `/song/download?url={songid}`
* Method: `GET`
* ResponseHeader: `MediaType:audio/mpeg`

### 전처리 요청
* URL: `/song/preprocess?songId={songId}`
* Method: `POST`
* Response: (HTTP Status 200) 비동기 적으로 요청을 보낸 후 바로 응답을 반환
  
### 음원 메타데이터 csv로 다운로드
* DESC: `추천알고리즘의 학습을 위한 데이터를 다운받기 위함`
* URL: `/song/download/csv2`
* Method: `GET`
* ResponseHeader: `MediaType:text/csv`
## DDSPController
### ptr파일 조회
* URL: `/ddsp/sampleVoiceList`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":{ptrID},
      "name":{ptr소유자 이름},
      "ptrUrl":{ptr파일 위치 경로}
    },
  ]
  ```
### 생성된 AI 커버곡 조회
* URL: `/ddsp/generatedSongList`
* Method: `GET`
* Response:
  ```
  [
    {
      "id":"생성된 커버곡 PK",
      "generatedUrl":"생성된 커버곡 Url",
      {원곡 정보}
    },
  ]
  ```
### AI 커버곡 생성 요청
* URL: `/ddsp/makesong`
* Method: `POST`
* RequestBody:
  ```
  {
    "targetVoiceId":"ptrID",
    "targetSongId":"만들고자하는 원본 노래의 ID"
  }
  ```
### AI 커버곡 삭제
* URL: `/ddsp/deleteSong/{resultSongId}`
* Method: `DELETE`
* Response: (HTTP Status 200)
  
## Spring 서버
<img src="https://github.com/ajou20658/song_server/assets/48721887/dac82913-f305-4fa6-b2a3-1a4a03aae8b2" width="100%"  style="background-color: #ffffff; padding-top:10px;">

## Spring과 Django의 요청 흐름도
<img src="https://github.com/ajou20658/song_server/assets/48721887/75aaddcd-134d-4a5d-8559-f2080a8c7719" width="100%"  style="background-color: #ffffff; padding-top:10px;">

## 파란학기 결과 템플릿
![image](https://github.com/ajou20658/song_server/assets/48721887/3db621bd-9296-4197-b38f-17e81d19b08f)

<img src="https://capsule-render.vercel.app/api?type=waving&color=037bfc&height=150&section=footer" />
