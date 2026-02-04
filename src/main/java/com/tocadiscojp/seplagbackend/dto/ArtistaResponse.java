package com.tocadiscojp.seplagbackend.dto;

import java.util.UUID;

import com.tocadiscojp.seplagbackend.enums.TipoArtista;

public record ArtistaResponse(
                UUID id,
                String nome,
                String generoMusical,
                TipoArtista tipo) {
}