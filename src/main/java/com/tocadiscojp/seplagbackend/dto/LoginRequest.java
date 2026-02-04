package com.tocadiscojp.seplagbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
                @Schema(description = "Login do usuário", example = "admin") @NotBlank String login,

                @Schema(description = "Senha de acesso ao sistema", example = "123456") @NotBlank String senha) {
}