package com.tocadiscojp.seplagbackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.tocadiscojp.seplagbackend.dto.AlbumRequest;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.model.Album;
import com.tocadiscojp.seplagbackend.model.Artista;
import com.tocadiscojp.seplagbackend.repository.AlbumRepository;
import com.tocadiscojp.seplagbackend.repository.ArtistaRepository;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;

    public AlbumService(AlbumRepository albumRepository, ArtistaRepository artistaRepository) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
    }

    public Page<AlbumResponse> listarTodos(Pageable pageable) {
        Page<Album> albuns = albumRepository.findAll(pageable);
        return albuns.map(this::toResponse);
    }

    @Transactional
    public AlbumResponse cadastrar(AlbumRequest request) {
        List<Artista> artistas = artistaRepository.findAllById(request.artistasIds());

        if (artistas.isEmpty()) {
            throw new RuntimeException("Nenhum artista encontrado com os IDs fornecidos");
        }

        Album album = new Album(request.titulo(), request.anoLancamento());

        for (Artista artista : artistas) {
            artista.adicionarAlbum(album);
        }

        Album albumSalvo = albumRepository.save(album);

        return toResponse(albumSalvo);
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