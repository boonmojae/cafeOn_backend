package com.b1a4.cafeOn.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CafeOn API",   // 여기서 제목 바꿔주면 Swagger 상단에 표시됨
                version = "v1.0.0",
                description = "CafeOn 프로젝트의 API 관련 문서입니다."
        )
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("CafeOn API")
                        .version("v1.0.0")
                        .description("CafeOn 프로젝트 API 명세"))
//                이 API들은 Bearer 인증이 필요함을 인식하도록 지정
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
//                        Swagger UI 상단에 🔒 Authorize 버튼을 표시하고, JWT를 입력할 수 있게 함
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
//                                        헤더 이름이 Authorization이고 형식이 Bearer <token> 임을 Swagger에게 알려줌
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}