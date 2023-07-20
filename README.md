# WEB회원가입 및 로그인 방법
## 1. 로그인 버튼의 uri를 "http://localhost:{PORT}/{callback주소}" 해당 주소로 설정한다
- 예시) https://kauth.kakao.com/oauth/authorize?client_id=f3474b073f9c02883e0b9ac53d7cbead&redirect_uri={콜백 주소}&response_type=code
## 2. 사용자가 로그인을 하게되면 콜백주소의 query부분에 authorization_code가 오게된다.
- 예시) http://localhost:{PORT}/{callback주소}/?code={authorization_code}
## 3. 백엔드로 인증코드를 보내면 jwt토큰을 반환해준다. 이후에는 해당 토큰을 이용하여 회원정보를 조회가능하다.

## 로그인
- POST/ http://{백엔드 주소}:{포트넘버}/api/auth/kakao
- body
- {
-   "authorizationCode":"콜백주소로 받은 authorization_code"
- }
## 회원 조회
- GET/ http://{백엔드 주소}:{포트넘버}/api/members/{반환 값으로 받은 jwt 토큰}
