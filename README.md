# 관리자 페이지
## Member-list

````
- URL : /admin/members
- METHOD : GET
- REPOSITORY에 등록된 모든 유저들 조회
````

## do-crawl
````
- URL : /admin/do-crawl
- METHOD : GET
- RESPONSE : JSON
- DESC : 12시 정각 나중에 cron 어노테이션사용하여 하루 넘어갈때마다 자동으로 top100 크롤링 할 예정
         현재는 cron기능 추가안함
````

# 유저 로그인 API
## web
````
- URL : /auth/login
- METHOD : POST
- RequestBody : "authorizationCode":"인가 코드"
- Response : {
    "httpStatus": "OK",
    "code": 0,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOTAxMzE5NTczIiwicm9sZXMiOiJST0xFX1VTRVIiLCJlbWFpbCI6InBkczA1MTc5QG5hdmVyLmNvbSIsIm5pY2tuYW1lIjoi7Jqw7JiBIiwiaWF0IjoxNjk0MTc4MDY1LCJleHAiOjE2OTY3NzAwNjV9.oeK8UQzSJOJDaPnyDNfR6VaPYGgv6PyjQhIDcdQaR4E",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOTAxMzE5NTczIiwiaWF0IjoxNjk0MTc4MDY1LCJleHAiOjE2OTY3NzAwNjV9.rB8u0bxhdYQ16NefT3pmo_rDBMI2HlhcjX3_xMPI8kY"
    }
}
- 로그인

````
## Android
````
-URL : /android/login
- METHOD : POST
- RequestBody : "authorizationCode":"인가 코드"
- Response : {
    "httpStatus": "OK",
    "code": 0,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOTAxMzE5NTczIiwicm9sZXMiOiJST0xFX1VTRVIiLCJlbWFpbCI6InBkczA1MTc5QG5hdmVyLmNvbSIsIm5pY2tuYW1lIjoi7Jqw7JiBIiwiaWF0IjoxNjk0MTc4MDY1LCJleHAiOjE2OTY3NzAwNjV9.oeK8UQzSJOJDaPnyDNfR6VaPYGgv6PyjQhIDcdQaR4E",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyOTAxMzE5NTczIiwiaWF0IjoxNjk0MTc4MDY1LCJleHAiOjE2OTY3NzAwNjV9.rB8u0bxhdYQ16NefT3pmo_rDBMI2HlhcjX3_xMPI8kY"
    }
}
````
# 유저 사용 API
## Member
````
- URL /member
- METHOD : GET
- RESPONSE : 유저 정보 json 반환
````

## 선호도 업데이트
````
- URL /update_prefer
- METHOD : POST
- PARAM : artist = 가수배열, genre = 장르배열, title = 제목배열
````
## 노래 업로드
````
- URL /upload
- METHOD : POST
- PARAM : file = {파일내용}
````


# melon기능 API
## showall
````
- URL : /v3/showall
- METHOD : GET
- DESC : 크롤링된 top100차트 조회(이후 어드민 페이지로 옮길 예정)
````
## charjson
````
- URL : /v3/charjson
- METHOD : GET
- DESC : 크롤링된 top100차트 json형태로 반환
````
## search
````
- URL : /v3/search?target=검색할 내용&mode=검색방법
- METHOD : GET
- PARAM(mode) : 0:전체 | 1:가수 | 2:제목 | 3:앨범제목 
- DESC : target과 mode를 받아서 요청에 맞는 크롤링을 하여 json형태로 반환한다.
````