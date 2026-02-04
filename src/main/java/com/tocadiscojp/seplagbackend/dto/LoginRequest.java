package com.tocadiscojp.seplagbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(description = "Login do usuário", example = "admin") String login,

        @Schema(description = "Senha de acesso ao sistema", example = "123456") String senha) {
}