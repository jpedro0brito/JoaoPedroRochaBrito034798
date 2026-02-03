package com.tocadiscojp.seplagbackend.dto;

import java.util.UUID;
import java.util.List;

public record AlbumResponse(
        UUID id,
        String titulo,
        Integer anoLancamento,
        String capaUrl,
        List<ArtistaResponse> artistas) {
}
