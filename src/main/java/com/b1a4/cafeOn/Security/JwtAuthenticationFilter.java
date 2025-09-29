package com.b1a4.cafeOn.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

//    1. 요청의 헤더에서 Bearer 토큰을 가져옴 (http요청의 헤더를 파싱해서 Bearer 토큰을 리턴)
    private String parseBearerToken(HttpServletRequest req) {
//        헤더에 Authorization: Bearer <token> 이런식으로 들어있을거임
        String bearerToken = req.getHeader("Authorization");    // Authorization 이란 key 값을 가진 문자열을 꺼냄
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {    // text값을 갖고있고, Bearer 로 시작하면
            return bearerToken.substring(7);    // "Bearer " 문자열의 길이가 7. 얘네를 뺀 뒤에 순수 토큰값만 리턴
        }

        return null;    // 형식이 맞지 않거나, 헤더가 없으면 null 반환
    }

    @Override
//    [필터의 핵심 메서드] 서블릿컨테이너가 요청마다 호출함.
//    2. 여기서 토큰을 꺼내 검증하고 컨텍스트를 채움
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain filterChain
    ) throws ServletException, IOException
    {
        try {
//            2-1. req에서 token 꺼내오기
            String token = parseBearerToken(req);   // 헤더에서 토큰을 파싱
            log.info("JwtAuthenticationFilter 실행 중...");

//            2-2. token 검사
//            if (token != null && !token.equalsIgnoreCase("null")) { // 정상 토큰만 처리
//                String userId = tokenProvider.validateAndGetUserId(token);  // 토큰을 검증하며 서명 위조나 만료면 예외가 발생, 정상이라면 sub로 넣어둔 userId를 꺼내 로그로 남김
//                log.info("Authenticated user id: "+userId);
//
////                2-3. 유효성 검사가 끝나면, 직전에 추출한 userId로 Spring Security의 인증 객체 생성
//                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                        // username이랑 password로 인증토큰객체 만드는 spring security 메소드
//                        userId, null, AuthorityUtils.NO_AUTHORITIES
//                );
//                log.info("authentication: "+authentication);

            if (token != null && !token.equalsIgnoreCase("null")) {
                // 1. TokenProvider로부터 Claims를 받아옵니다. (userId가 아님)
                io.jsonwebtoken.Claims claims = tokenProvider.validateAndGetClaims(token);
                String userId = claims.getSubject();
                String role = claims.get("auth", String.class);

                java.util.Collection<? extends GrantedAuthority> authorities =
                        java.util.Collections.singletonList(new SimpleGrantedAuthority(role));

                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                );

//                SecurityContextHolder: Spring Security에서 인증된 사용자 정보를 저장하는 곳
//                -> 이 요청을 처리하는 동안 사용자 인증 정보를 여기에 보관해 두자)
//                2-4-1. 빈 SecurityContext 만듬
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
//                2-4-2. SecurityContext에 authentication으로 심음 => 이후부터는 인증된 사용자로 인식됨
                securityContext.setAuthentication(authentication);
//                2-4-3. 그 SecurityContext 자체를 전역 저장소에 심음=> 이후 컨트롤러, 서비스계층 등에서
//                SecurityContextHolder.getContext().getAuthentication()를 통해 인증된 사용자를 꺼낼 수 있게 됨
                SecurityContextHolder.setContext(securityContext);
            }
        } catch (Exception e) {
//            logger? spring security 필터 클래스에 기본 내장된 로그
            logger.error("Could not set user authentication is security context", e);   // OncePerRequestFilter에서 가지고오는 객체
        }
        
//        3. 다음 필터/컨트롤러로 넘김 (Node.js의 next() 와 완전히 같음)
        filterChain.doFilter(req, res);
    }
}