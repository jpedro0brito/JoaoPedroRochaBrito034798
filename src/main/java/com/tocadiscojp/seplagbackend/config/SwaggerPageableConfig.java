package com.tocadiscojp.seplagbackend.config;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerPageableConfig {

    @Bean
    public OperationCustomizer customizePageable() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(parameter -> {
                    switch (parameter.getName()) {
                        case "page" -> parameter
                                .description("Índice da página que você deseja recuperar (inicia em 0)")
                                .example("0");
                        case "size" -> parameter
                                .description("Quantidade de registros por página")
                                .example("10");
                        case "sort" -> parameter
                                .description("Critério de ordenação no formato: `propriedade,(asc|desc)`. " +
                                        "Exemplo: `titulo,asc`. Múltiplos critérios são suportados.")
                                .example("titulo,asc");
                    }
                });
            }
            return operation;
        };
    }
}