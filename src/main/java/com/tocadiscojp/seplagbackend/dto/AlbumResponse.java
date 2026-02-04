package com.tocadiscojp.seplagbackend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AlbumResponse(
                @Schema(description = "ID único do álbum no sistema", example = "d9b2b6d0-...") UUID id,

                @Schema(description = "Título do álbum", example = "Clube da Esquina") String titulo,

                @Schema(description = "Ano de lançamento", example = "1972") Integer anoLancamento,

                @Schema(description = "URL da imagem de capa", example = "https://localhost:8080/capas/album123.jpg") String capaUrl,

                List<ArtistaResponse> artistas) {
}