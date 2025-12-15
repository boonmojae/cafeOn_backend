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

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/stomp/**", "/stomp/chats/**").permitAll()
                        // 공개 엔드포인트
                        .requestMatchers("/", "/api/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/api-docs", "/api-docs-json").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 이하 기존 정책 유지
                        .requestMatchers("/api/chat/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cafes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/posts").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/reports").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/comments").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/comment/*/reports").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cafes/*/reviews").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/reviews/*/reports").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/notifications/unread").hasRole("USER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/mypage/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/qna/questions").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/qna/questions", "/api/qna/questions/**").permitAll()
                        .anyRequest().authenticated()
                );

        // CORS 필터 이후에 JWT 필터 동작
        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 운영에서는 구체 도메인/IP로 제한 권장
        // feedback
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        config.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
