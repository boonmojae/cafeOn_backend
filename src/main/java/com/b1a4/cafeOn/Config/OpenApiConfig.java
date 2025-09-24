package com.b1a4.cafeOn.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
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
}