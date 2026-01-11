package com.example.moamoa_backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    /**
     * Create and configure the application's OpenAPI definition with API metadata, a root server, and a JWT bearer security scheme.
     *
     * @return the configured OpenAPI instance containing Info (title/description/version), a server with URL "/", a SecurityRequirement referencing the "JWT TOKEN" scheme, and Components registering the "JWT TOKEN" HTTP bearer (JWT) SecurityScheme.
     */
    @Bean
    public OpenAPI swagger() {
        Info info = new Info()
                .title("MOAMOA API")
                .description("UMC 9기 MOAMOA 프로젝트 API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";

        // API 요청헤더에 인증정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}