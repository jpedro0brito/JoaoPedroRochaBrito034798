package com.tocadiscojp.seplagbackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.model.Album;
import com.tocadiscojp.seplagbackend.repository.AlbumRepository;

@Service
public class AlbumService {

    private final AlbumRepository repository;

    public AlbumService(AlbumRepository repository) {
        this.repository = repository;
    }

    public Page<AlbumResponse> listarTodos(Pageable pageable) {
        Page<Album> albuns = repository.findAll(pageable);
        return albuns.map(this::toResponse);
    }

    private AlbumResponse toResponse(Album entity) {
        List<ArtistaResponse> artistasDto = entity.getArtistas().stream()
                .map(a -> new ArtistaResponse(a.getId(), a.getNome(), a.getGeneroMusical()))
                .collect(Collectors.toList());

        return new AlbumResponse(
                entity.getId(),
                entity.getTitulo(),
                entity.getAnoLancamento(),
                entity.getCapaUrl(),
                artistasDto);
    }
}