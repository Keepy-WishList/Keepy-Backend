package com.keepy.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "BearerAuth";

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Keepy API")
                .description("Keepy 쇼핑 위시리스트 서비스 API 문서")
                .version("v1.0.0");
    }

    private List<Server> servers() {
        if ("prod".equals(activeProfile)) {
            return List.of(
                    new Server().url("https://api.keepy.com").description("Production")
            );
        }
        return List.of(
                new Server().url("http://localhost:8080").description("Local")
        );
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("로그인 후 발급받은 Access Token을 입력하세요. (Bearer 접두사 불필요)");
    }
}
