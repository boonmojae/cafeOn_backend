package com.b1a4.cafeOn.Security;

import com.b1a4.cafeOn.configs.jwt.JwtProperties;
import com.b1a4.cafeOn.entity.UserEntity;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

// 사용자 정보를 받아 JWT 생성하는 클래스
@Service    // 서비스계층 컴포넌트로 등록해서 다른 곳에서 주입받아서 쓰기 위함
@Slf4j
public class TokenProvider {
//    [before] JWT 서명에 사용되는 비밀키 (일단은 하드코딩 했지만, [after]로 바꾸었음)
//    private static final String SECRET_KEY = "cafe-on-kimdoi1004";

    //    [after] JwtProperties 클래스 이용해 설정 파일 값 불러오기
    @Autowired
    private JwtProperties jwtProperties;

    //    1. Access, Refresh Token 둘 다 발급 (로그인 시)
    public Map<String, String> issueTokens(UserEntity userEntity) {
        String accessToken = issueAccessToken(userEntity);
        String refreshToken = issueRefreshToken(userEntity);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    //      1-1. Access Token만 발급 (30분 뒤 만료시간만 다름)
    public String issueAccessToken(UserEntity userEntity) {
        // Access Token 발급
//        JWT Access 토큰 만료시간을 현재시각으로부터 30분 뒤 만료되는 시각으로 계산
//        -> 30분 뒤 만료된 이후엔, RefreshToken을 서버에 보내서 Access Token을 새로 받는 요청을 함
        Date expiryDate = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));

//        JWT 토큰 생성
        return Jwts.builder()   // jwt header(암호화 알고리즘, 타입) 에 들어갈 내용 및 서명하기 위한 SECRET KEY
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey()) // HMAC(Hash-based Message Authentication Code)-SHA512 알고리즘
                .setSubject(String.valueOf(userEntity.getUserId())) // sub: 토큰제목(여기서는 userId)
                .setIssuer("cafeOn")                                // iss: 토큰 발급자
                .setIssuedAt(new Date())                            // iat: 토큰이 발급된 시간
                .setExpiration(expiryDate)                          // exp: 토큰 만료 시간
                .claim("tokenType", "access")               // tokenType 클레임 추가
                .claim("role", userEntity.getRole().name())     // role 클레임 추가
                .compact(); // 토큰 생성해주세요! -> "header.payload.signature" 토큰 문자열 최종 생성(리턴타입 그래서 String)
    }

    //      1-2. Refresh Token만 발급 (14일 뒤 만료시간만 다름)
    public String issueRefreshToken(UserEntity userEntity) {   // Refresh Token 발급 (토큰 갱신 시)
        Date expiryDate = Date.from(Instant.now().plus(14, ChronoUnit.DAYS));   // 14일 뒤 만료

        log.info("발급된 RefreshToken 만료시간: {}", expiryDate);

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                .setSubject(String.valueOf(userEntity.getUserId()))
                .setIssuer("cafeOn")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("tokenType", "refresh")               // tokenType 클레임 추가
                .claim("role", userEntity.getRole().name())     // role 클레임 추가
                .compact();
    }

//    ---------------------------------------------------------------

    //    2. 토큰 디코딩 및 파싱 & 토큰 위조 여부를 확인 -> 사용자의 id 리턴
//    => 클라이언트가 보낸 토큰이 유효한지 검증하고, <userId,role> 맵을 반환함
    public Map<String, String> validateAndExtractClaims(String token, String expectedTokenType) {
        try {
//        2-1. 79번째줄 parseClaimsJws메소드가 Base64로 디코딩 및 파싱
//        - header, payload를 setSigningKey로 넘어온 SECRET KEY를 사용해 서명한 후, token의 서명과 비교
//        - 서명이 위조되거나 만료된 토큰이라면 -> 예외 발생
//        - 위조되지 않았다면 페이로드(Claims) 리턴
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())    // 서명 검증에 사용할 비밀키 지정
                    .parseClaimsJws(token)                          // JWT Base64로 디코딩 및 파싱 -> header, payload, signature 검증
//                만료되었거나 위조된 경우 -> ExpiredJwtException, SignatureException 같은 에외 발생시킴
                    .getBody();

//            2-2. 토큰 타입 검사
            Object type = claims.get("tokenType");
            if (type == null || !expectedTokenType.equals(type.toString())) {
                log.warn("토큰 타입 불일치 (예상: {}, 실제: {})", expectedTokenType, type);
                return null;
            }

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            return Map.of(
                    "userId", userId,
                    "role", role
            );
        } catch (ExpiredJwtException e) {
            log.warn("만료된 토큰입니다: {}", token, e);    // ,e 때문에 에러메시지+스택트레이스까지 같이 로그에 찍힘
            return null;
        } catch (SignatureException e) {
            log.warn("서명이 위조된 토큰입니다: {}", token, e);
            return null;
        } catch (Exception e) {
            log.warn("유효하지 않은 토큰입니다: {}", token, e);
            return null;
        }
    }
}