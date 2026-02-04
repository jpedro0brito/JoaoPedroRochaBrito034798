package com.tocadiscojp.seplagbackend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.tocadiscojp.seplagbackend.dto.AlbumRequest;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.model.Album;
import com.tocadiscojp.seplagbackend.model.Artista;
import com.tocadiscojp.seplagbackend.repository.AlbumRepository;
import com.tocadiscojp.seplagbackend.repository.ArtistaRepository;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistaRepository artistaRepository;
    private final MinioService minioService;
    private final SimpMessagingTemplate messagingTemplate;

    public AlbumService(
            AlbumRepository albumRepository,
            ArtistaRepository artistaRepository,
            MinioService minioService,
            SimpMessagingTemplate messagingTemplate) {
        this.albumRepository = albumRepository;
        this.artistaRepository = artistaRepository;
        this.minioService = minioService;
        this.messagingTemplate = messagingTemplate;
    }

    public Page<AlbumResponse> listarTodos(Pageable pageable, TipoArtista tipo, String nomeArtista) {
        String filtroNome = null;

        if (nomeArtista != null && !nomeArtista.isBlank()) {
            filtroNome = "%" + nomeArtista + "%";
        }

        Page<Album> paginaAlbuns = albumRepository.buscarComFiltros(tipo, filtroNome, pageable);
        return paginaAlbuns.map(this::toResponse);
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

        String mensagem = "Novo álbum cadastrado: " + albumSalvo.getTitulo();
        messagingTemplate.convertAndSend("/topic/albuns", mensagem);

        return toResponse(albumSalvo);
    }

    @Transactional
    public AlbumResponse atualizarCapa(UUID id, MultipartFile file) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado"));

        String nomeDoArquivo = minioService.enviarArquivo(file);

        album.setCapaUrl(nomeDoArquivo);
        albumRepository.save(album);

        return toResponse(album);
    }

    @Transactional
    public AlbumResponse atualizar(UUID id, AlbumRequest request) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado"));

        album.setTitulo(request.titulo());
        album.setAnoLancamento(request.anoLancamento());

        if (request.artistasIds() != null && !request.artistasIds().isEmpty()) {
            List<Artista> novosArtistas = artistaRepository.findAllById(request.artistasIds());

            for (Artista a : novosArtistas) {
                a.adicionarAlbum(album);
            }
        }

        return toResponse(albumRepository.save(album));
    }

    private AlbumResponse toResponse(Album entity) {
        List<ArtistaResponse> artistasDto = entity.getArtistas().stream()
                .map(a -> new ArtistaResponse(a.getId(), a.getNome(), a.getGeneroMusical(), a.getTipo()))
                .collect(Collectors.toList());

        String urlAssinada = null;
        if (entity.getCapaUrl() != null && !entity.getCapaUrl().isEmpty()) {
            urlAssinada = minioService.obterUrlPreAssinada(entity.getCapaUrl());
        }

        return new AlbumResponse(
                entity.getId(),
                entity.getTitulo(),
                entity.getAnoLancamento(),
                urlAssinada,
                artistasDto);
    }
}