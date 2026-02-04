package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.ArtistaRequest;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.model.Artista;
import com.tocadiscojp.seplagbackend.repository.ArtistaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ArtistaService {

    private final ArtistaRepository repository;

    public ArtistaService(ArtistaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ArtistaResponse salvar(ArtistaRequest request) {
        Artista artista = new Artista(request.nome(), request.generoMusical(), request.tipo());
        return toResponse(repository.save(artista));
    }

    @Transactional
    public ArtistaResponse alterar(UUID id, ArtistaRequest request) {
        Artista artista = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado"));

        artista.setNome(request.nome());
        artista.setGeneroMusical(request.generoMusical());
        artista.setTipo(request.tipo());

        return toResponse(repository.save(artista));
    }

    public List<ArtistaResponse> listar(TipoArtista tipo, String nome, String ordem) {
        Sort.Direction direction = "desc".equalsIgnoreCase(ordem) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, "nome");

        return repository.buscarComFiltros(tipo, nome, sort).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void removerOuDesativar(UUID id) {
        Artista artista = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado"));

        if (artista.getAlbuns() == null || artista.getAlbuns().isEmpty()) {
            repository.delete(artista);
        } else {
            artista.setAtivo(false);

            artista.getAlbuns().forEach(album -> {
                album.setAtivo(false);
            });

            repository.save(artista);
        }
    }

    private ArtistaResponse toResponse(Artista artista) {
        return new ArtistaResponse(
                artista.getId(),
                artista.getNome(),
                artista.getGeneroMusical(),
                artista.getTipo());
    }
}