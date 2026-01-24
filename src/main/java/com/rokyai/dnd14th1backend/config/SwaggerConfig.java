package com.rokyai.dnd14th1backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080"); // API 서버 주소 지정

        return new OpenAPI()
                .info(apiInfo())
                //                .addSecurityItem(
                //                        new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // // 전역 인증 설정
                //                .components(
                //                        new Components()
                //                                .addSecuritySchemes(
                //                                        SECURITY_SCHEME_NAME,
                //                                        new SecurityScheme()
                //                                                .name(SECURITY_SCHEME_NAME)
                //                                                .type(SecurityScheme.Type.HTTP)
                //                                                .scheme("bearer")
                //                                                .bearerFormat("JWT") // 선택 사항
                //                                        ))
                .servers(List.of(server));
    }

    private Info apiInfo() {
        return new Info().title("DND API 1조 Swagger 문서").version("0.0.1");
    }
}
