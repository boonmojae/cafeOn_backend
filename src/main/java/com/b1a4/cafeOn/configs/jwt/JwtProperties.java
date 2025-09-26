package com.b1a4.cafeOn.configs.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("jwt") // 자바클래스에 property 값을 가져와서 사용하는 어노테이션
// application.properties 파일에 있는 설정 값을 가져오고자 하는 클래스
public class JwtProperties {
    private String issuer;  // application.properties의 jwt.issuer를 찾아 자동으로 매칭
    private String secretKey;   // application.properties의 jwt.secret_key를 찾아 자동으로 매칭
}