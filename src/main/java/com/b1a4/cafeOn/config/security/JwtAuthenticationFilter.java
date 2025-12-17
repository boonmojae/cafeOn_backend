package com.b1a4.cafeOn.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    private String parseBearerToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = req.getRequestURI();

        // 로그 레벨 최적화: DEBUG 환경에서만 로그 출력
        // logback/log4j 설정에 따라 DEBUG 레벨일 때만 출력됩니다.
        if (log.isDebugEnabled()) {
            log.debug("JwtAuthenticationFilter 실행 중... path={}", path);
        }

        try {
            // 불필요한 경로 필터링
            if (path.startsWith("/actuator/") || path.equals("/api/auth/refresh")) {
                filterChain.doFilter(req, res);
                return;
            }

            // 추가 필터링 (예: Swagger, 정적 파일) 선택
            if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
                filterChain.doFilter(req, res);
                return;
            }

            // 토큰 검증 및 인증 로직 시작
            String token = parseBearerToken(req);
            if (token != null && !token.equalsIgnoreCase("null")) {

                Map<String, String> claims = tokenProvider.validateAndExtractClaims(token, "access");
                if (claims != null) {
                    String userId = claims.get("userId");
                    String role = claims.get("role");

                    AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            AuthorityUtils.createAuthorityList("ROLE_" + role.toUpperCase())
                    );

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);

                    // 인증 성공 로그: INFO 레벨로 유지
                    log.info("인증 성공 - userId: {}, role: {}", userId, role);
                }
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(req, res);
    }
}