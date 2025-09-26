package com.b1a4.cafeOn.configs;

import com.b1a4.cafeOn.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration  // 스프링컨테이너에게 해당 클래스가 Bean 정의를 포함한 설정클래스임을 알림
@EnableWebSecurity  // Spring Security 활성화
@Slf4j
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean   // Bean으로 등록했기때문에, 스프링이 PasswordEncoder 객체를 관리해서,
    // 다른곳에서 @Autowired PasswordEncoder passwordEncoder 선언 시, 스프링이 컨테이너 안의 이 Bean(BcryptPasswordEncoder)을 자동으로 찾아 주입함
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 직접 new BCryptPasswordEncoder()로 생성하지 않고도, 스프링이 관리하는 Bean을 가져다 쓸 수 있게 함
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults())   // cors 기본으로 설정
                .csrf(CsrfConfigurer::disable)  // csrf(공격 종류 중 1. 크로스사이트 요청위조 공격)를 disable 설정
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // session 기반이 아니므로 무상태(STATELESS) 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs", "/api-docs-json")  // 요청 경로가 일치하는 애들한테는
                        .permitAll()    // /, /api/auth/** 경로는 인증 안해도 되게 모두 허용하겠다!!(이코드 안쓰면 우리코드랑 관련없는 무슨 security 기본 로그인화면뜸)
                        .anyRequest().authenticated()); // 그 이외의 모든 경로는 인증 해야됨

//        filter 등록: 매 요청마다 CorsFilter를 실행한 후에 -> JwtAuthenticationFilter{}를 실행되게 순서 세팅
        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();
    }

//    cors 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

//        모든 출처, 메소드, 헤더에 대해 허용하는 cors 설정
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}