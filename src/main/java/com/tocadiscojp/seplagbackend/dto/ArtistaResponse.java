package com.tocadiscojp.seplagbackend.dto;

import java.util.UUID;

public record ArtistaResponse(
        UUID id,
        String nome,
        String generoMusical) {
}