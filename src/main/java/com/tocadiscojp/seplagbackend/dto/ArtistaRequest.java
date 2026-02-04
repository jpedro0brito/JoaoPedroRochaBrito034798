package com.tocadiscojp.seplagbackend.dto;

import com.tocadiscojp.seplagbackend.enums.TipoArtista;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ArtistaRequest(
        @Schema(description = "Nome do artista", example = "The Beatles") @NotBlank String nome,
        @Schema(description = "Gênero musical do artista", example = "Rock") @NotBlank String generoMusical,
        @Schema(description = "Tipo de artista (SOLO/BANDA)", example = "SOLO") @NotNull TipoArtista tipo) {
}
