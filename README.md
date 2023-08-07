# 관리자 페이지
## Member-list

````
- URL : /admin/members
- METHOD : GET
- REPOSITORY에 등록된 모든 유저들 조회
````

## Token-lst
````
- URL : /admin/tokens
- METHOD : GET
- DESC : 회원가입한 유저들 kakao accesstoken조회
         이후 회원 탈퇴구현시 사용예정
- REPOSITORY에 등록된 모든 KAKAOOAUTH토큰 조회가능
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
## Login
````
- URL : /v2/login
- METHOD : POST
- RequestParam : ?authorizationCode={인가 코드}
````
- Response : jwtCookie, jwtRefresh
- expire시간만큼 쿠키값 저장



## AndroidLogin
````
-URL : /v3/login
````
# 유저 사용 API
## Member
````
- URL /member
- METHOD : GET
- 사용자의 쿠키값을 파싱하여 유저 정보 json으로 반환
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