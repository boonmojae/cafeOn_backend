package com.b1a4.cafeOn.config;

import com.b1a4.cafeOn.config.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
//    Spring Security가 보안필터체인(Security Filter Chain)을 구성함
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults())   // cors 기본으로 설정
                .csrf(CsrfConfigurer::disable)  // csrf(공격 종류 중 1. 크로스사이트 요청위조 공격)를 disable 설정
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // session 기반이 아니므로 무상태(STATELESS) 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs", "/api-docs-json", "/stomp/chats/**", "/chat-test.html")  // 요청 경로가 일치하는 애들한테는
                        .permitAll()    // /, /api/auth/** 경로는 인증 안해도 되게 모두 허용하겠다!!(이코드 안쓰면 우리코드랑 관련없는 무슨 security 기본 로그인화면뜸)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/posts").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/comments").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").hasRole("USER")
                        // 관리자
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 마이페이지 (USER)
                        .requestMatchers("/api/mypage/**").hasRole("USER")
                        // QnA 작성 (USER)
                        .requestMatchers(HttpMethod.POST, "/api/qna/questions").hasRole("USER")
                        // QnA 조회 (공개)
                        .requestMatchers(HttpMethod.GET, "/api/qna/questions", "/api/qna/questions/**").permitAll()

                        .anyRequest().authenticated()); // 그 이외의 모든 경로는 인증 해야됨

//        filter 등록: 매 요청마다 (1)CorsFilter를 실행한 후에 -> (2)JwtAuthenticationFilter{}를 실행되게 순서 세팅
        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();    // 앱 시작 시 한 번 호출되어 "필터 체인 구성"만 함
//        @Bean메서드에서 완성된 SecurityFilterChain 빈을 반환해야 하기 때문에, 이 반환값을 스프링이 받아서 보안 필터링의 기준으로 사용
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    //    cors 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

//        모든 출처, 메소드, 헤더에 대해 허용하는 cors 설정
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}