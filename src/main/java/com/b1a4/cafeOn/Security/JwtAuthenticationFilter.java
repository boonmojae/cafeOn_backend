package com.b1a4.cafeOn.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component  // 스프링컨테이너한테 Bean으로 등록해서 의존성 주입받으려고
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Servlet Filter(미들웨어)를 구현한 것
//    OncePerRequestFilter클래스를 상속받는 JwtAuthenticationFilter 구현
//    - OncePerRequestFilter 클래스 : 한 요청당 반드시 한 번만 실행됨
    @Autowired
    private TokenProvider tokenProvider;

//    요청의 헤더에서 Bearer 토큰을 가져옴 (http요청의 헤더를 파싱해서 Bearer 토큰을 리턴)
    private String parseBearerToken(HttpServletRequest req) {
//        헤더에 Authorization: Bearer <token> 이런식으로 들어있을거임
        String bearerToken = req.getHeader("Authorization");    // Authorization 이란 key 값을 가진 문자열을 꺼냄
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {    // text값을 갖고있고, Bearer 로 시작하면
            return bearerToken.substring(7);    // "Bearer " 문자열의 길이가 7. 얘네를 뺀 뒤에 순수 토큰값만 리턴
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
//            req에서 token 꺼내오기
            String token = parseBearerToken(req);
            log.info("JwtAuthenticationFilter 실행 중...");

//            token 검사
            if (token != null && !token.equalsIgnoreCase("null")) {
                String userId = tokenProvider.validateAndGetUserId(token);
                log.info("Authenticated user id: "+userId);
            }

        } catch (Exception e) {

        }
    }
}
