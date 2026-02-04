package com.tocadiscojp.seplagbackend.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlbumRequest(
        @Schema(description = "Título oficial do álbum", example = "The Dark Side of the Moon") @NotBlank String titulo,

        @Schema(description = "Ano em que o álbum foi lançado", example = "1973") @NotBlank Integer anoLancamento,

        @Schema(description = "Lista de IDs dos artistas vinculados", example = "['550e8400-e29b-41d4-a716-446655440000']") @NotNull List<UUID> artistasIds) {
}
