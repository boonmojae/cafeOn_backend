package com.b1a4.cafeOn.Security;

import com.b1a4.cafeOn.Entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

// 사용자 정보를 받아 JWT 생성하는 클래스
@Service    // 서비스계층 컴포넌트로 등록해서 다른 곳에서 주입받아서 쓰기 위함
@Slf4j
public class TokenProvider {
//    [before] JWT 서명에 사용되는 비밀키 (일단은 하드코딩, TODO 나중에 [after]로 바꿀예정)
    private static final String SECRET_KEY = "cafe-on-kimdoi1004";

//    Access, Refresh Token 둘 다 발급 (로그인 시)
    public Map<String, String> issueTokens(UserEntity userEntity) {
        String accessToken = issueAccessToken(userEntity);
        String refreshToken = issueRefreshToken(userEntity);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    private String issueAccessToken(UserEntity userEntity) {
        // Access Token 발급
//        JWT 토큰 만료시간을 현재시각으로부터 30분 뒤 만료되는 시각으로 계산
        Date expiryDate = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));

//        JWT 토큰 생성
        return Jwts.builder()   // jwt header(암호화 알고리즘, 타입) 에 들어갈 내용 및 서명하기 위한 SECRET KEY
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY) // HMAC(Hash-based Message Authentication Code)-SHA512 알고리즘
                .setSubject(String.valueOf(userEntity.getUserId())) // sub: 토큰제목(여기서는 userId)
                .setIssuer("cafeOn")                                // iss: 토큰 발급자
                .setIssuedAt(new Date())                            // iat: 토큰이 발급된 시간
                .setExpiration(expiryDate)                          // exp: 토큰 만료 시간
                .compact(); // 토큰 생성해주세요! -> "header.payload.signature" 토큰 문자열 최종 생성(리턴타입 그래서 String)
    }

    private String issueRefreshToken(UserEntity userEntity) {   // Refresh Token 발급 (토큰 갱신 시)
        Date expiryDate = Date.from(Instant.now().plus(14, ChronoUnit.DAYS));   // 14일 뒤 만료
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .setSubject(userEntity.getUserId())
                .setIssuer("cafeOn")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .compact();
    }
}