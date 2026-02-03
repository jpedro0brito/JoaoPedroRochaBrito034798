package com.tocadiscojp.seplagbackend.dto;

import java.util.List;
import java.util.UUID;

public record AlbumRequest(
        String titulo,
        Integer anoLancamento,
        List<UUID> artistasIds) {
}
