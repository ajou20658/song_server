//package com.example.cleancode.filter;
//
//import com.example.cleancode.login.JpaRepository.MemberRepository;
//import com.example.cleancode.login.dto.JwtDto;
//import com.example.cleancode.login.dto.MemberDto;
//import com.example.cleancode.login.entity.Member;
//import com.example.cleancode.login.service.LoginService;
//import com.example.cleancode.utils.jwt.JwtTokenProvider;
//import jakarta.servlet.*;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.core.Authentication;
////import org.springframework.security.core.authority.SimpleGrantedAuthority;
////import org.springframework.security.core.context.SecurityContextHolder;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.stereotype.Component;
//import org.springframework.util.PatternMatchUtils;
//
//import java.io.IOException;
//import java.util.*;
//
//@Slf4j
//@RequiredArgsConstructor
//public class JwtTokenFilter implements Filter {
//    private final JwtTokenProvider jwtTokenProvider;
//    private final MemberRepository memberRepository;
//    private final LoginService loginService;
//    @Value("${jwt.token.expiration-time}")
//    private Long tokenMillisecond;
//    @Value("${jwt.token.refresh-expiration-time}")
//    private Long refreshMillisecond;
//    public static final String[] whitelist = {"/favicon.ico","/v2/login","/v2/test","/admin/members","/admin/tokens","/v3","/v3/upload","/v3/form"};
//    public static final String URL ="http://localhost:3000/mypage";
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        HttpServletRequest req = (HttpServletRequest)request;
//        Cookie[] cookies = req.getCookies();
//        log.info("jwt filter");
//        if(cookies!=null){
//            Map<String, String> cookieMap = new HashMap<>();
//            List<Cookie> cookieList = Arrays.asList(cookies);
//            for (Cookie cookie : cookieList) {
//                cookieMap.put(cookie.getName(), cookie.getValue());
//                log.info("1");
//            }
//            try {
//                String token = cookieMap.get("jwtCookie");
//                String refresh = cookieMap.get("jwtRefresh");
//                JwtDto jwtDto = new JwtDto(token, refresh);
//
//                //jwt 만료되면
//                if(!jwtTokenProvider.validateToken(jwtDto)){
//                    //refresh 는 만료안되면 새로 발급
//                    HttpServletResponse res = (HttpServletResponse) response;
//                    if(jwtTokenProvider.validateRefresh(jwtDto)){
//                        Optional<Member> member = memberRepository.findById(jwtTokenProvider.getId(jwtDto));
//                        JwtDto jwtDto1 = jwtTokenProvider.generate(member.get().getId(),Collections.singletonList(member.get().getRole()));
//                        loginService.setCookie(res,jwtDto1);
//                    }else{
//                        //refresh까지 만료된경우 재 로그인 필요
//                        //프론트의 redirect url로 보내기
////                        res.sendRedirect(URL);
//                        log.info("토큰 없음");
//                    }
//                }
//
//
//                Long userId = jwtTokenProvider.getId(jwtDto);
//                String role = jwtTokenProvider.getRole(jwtDto);
//                Optional<Member> member = memberRepository.findById(userId);
//                Member tmp = member.get();
//                MemberDto dto = new MemberDto(userId,tmp.getEmail(),tmp.getNickname(),tmp.getPreferences());
//                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//
//                authorities.add(new SimpleGrantedAuthority(role));
//
//                //쿠키있으면 userRepository에 저장된 권한 부여
//                Authentication auth = new UsernamePasswordAuthenticationToken(dto, null, authorities);
//                SecurityContextHolder.getContext().setAuthentication(auth);
//                log.info("filter o.k.");
//            }catch(Exception ex){
//                log.error("No cookie"+ex.getMessage());
//            }
//        }
//        chain.doFilter(request, response);
//    }
//}
