package com.tocadiscojp.seplagbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento Musical - Seplag Backend 2026")
                        .description("### Instruções de Acesso\n" +
                                "Para testar os endpoints protegidos:\n" +
                                "1. Realize o login no endpoint **/auth/login**\n" +
                                "2. Copie o **accessToken** recebido\n" +
                                "3. Clique no botão **Authorize** ao lado\n" +
                                "4. Cole o token no campo **Value**"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description(
                                        "Insira apenas o token JWT. O prefixo 'Bearer ' será adicionado automaticamente.")));
    }
}