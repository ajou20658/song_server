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
````

- REPOSITORY에 등록된 모든 KAKAOOAUTH토큰 조회가능

# 유저 로그인 API
## Login
````
- URL : /v2/login
- METHOD : POST
- RequestParam : ?authorizationCode={인가 코드}
````
- Response : jwtCookie, jwtRefresh
- expire시간만큼 쿠키값 저장

## Member
````agsl
- URL /v2/member
- METHOD : GET
````
- RequestParam : Null
- Response : Member Info Json
- 사용자의 쿠키값을 파싱하여 정보 반환