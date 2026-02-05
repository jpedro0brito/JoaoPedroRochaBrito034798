package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.AlbumRequest;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.model.Album;
import com.tocadiscojp.seplagbackend.model.Artista;
import com.tocadiscojp.seplagbackend.repository.AlbumRepository;
import com.tocadiscojp.seplagbackend.repository.ArtistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes do AlbumService")
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ArtistaRepository artistaRepository;

    @Mock
    private MinioService minioService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AlbumService albumService;

    private Album album;
    private Artista artista;
    private UUID albumId;
    private UUID artistaId;

    @BeforeEach
    void setUp() {
        albumId = UUID.randomUUID();
        artistaId = UUID.randomUUID();

        artista = new Artista("The Beatles", "Rock", TipoArtista.BANDA);
        artista.setId(artistaId);

        album = new Album("Abbey Road", 1969);
        album.setId(albumId);
        album.getArtistas().add(artista);
    }

    @Test
    @DisplayName("Deve listar todos os álbuns com filtros aplicados")
    void listarTodos_ComFiltros_RetornaPaginaCorreta() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        TipoArtista tipo = TipoArtista.BANDA;
        String nomeArtista = "Beatles";
        String filtroNome = "%" + nomeArtista + "%";

        List<Album> albuns = Arrays.asList(album);
        Page<Album> paginaAlbuns = new PageImpl<>(albuns, pageable, 1);

        when(albumRepository.buscarComFiltros(tipo, filtroNome, pageable))
                .thenReturn(paginaAlbuns);
        when(minioService.obterUrlPreAssinada(any())).thenReturn(null);

        // Act
        Page<AlbumResponse> resultado = albumService.listarTodos(pageable, tipo, nomeArtista);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).titulo()).isEqualTo("Abbey Road");
        verify(albumRepository).buscarComFiltros(tipo, filtroNome, pageable);
    }

    @Test
    @DisplayName("Deve listar todos os álbuns sem filtros")
    void listarTodos_SemFiltros_RetornaTodosAlbuns() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Album> albuns = Arrays.asList(album);
        Page<Album> paginaAlbuns = new PageImpl<>(albuns, pageable, 1);

        when(albumRepository.buscarComFiltros(null, null, pageable))
                .thenReturn(paginaAlbuns);
        when(minioService.obterUrlPreAssinada(any())).thenReturn(null);

        // Act
        Page<AlbumResponse> resultado = albumService.listarTodos(pageable, null, null);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        verify(albumRepository).buscarComFiltros(null, null, pageable);
    }

    @Test
    @DisplayName("Deve cadastrar álbum com artistas válidos e enviar mensagem WebSocket")
    void cadastrar_ComArtistasValidos_SalvaAlbumEEnviaMensagemWebSocket() {
        // Arrange
        AlbumRequest request = new AlbumRequest("Abbey Road", 1969, Arrays.asList(artistaId));
        List<Artista> artistas = Arrays.asList(artista);

        when(artistaRepository.findAllById(request.artistasIds())).thenReturn(artistas);
        when(albumRepository.save(any(Album.class))).thenReturn(album);
        when(minioService.obterUrlPreAssinada(any())).thenReturn(null);

        // Act
        AlbumResponse resultado = albumService.cadastrar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.titulo()).isEqualTo("Abbey Road");
        assertThat(resultado.anoLancamento()).isEqualTo(1969);
        verify(albumRepository).save(any(Album.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/albuns"), contains("Abbey Road"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar álbum sem artistas")
    void cadastrar_SemArtistas_LancaException() {
        // Arrange
        AlbumRequest request = new AlbumRequest("Abbey Road", 1969, Arrays.asList(artistaId));
        when(artistaRepository.findAllById(request.artistasIds())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThatThrownBy(() -> albumService.cadastrar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nenhum artista encontrado");

        verify(albumRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @DisplayName("Deve atualizar capa do álbum existente")
    void atualizarCapa_ComAlbumExistente_AtualizaCapaUrl() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        String nomeArquivo = "capa-123.jpg";

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(minioService.enviarArquivo(file)).thenReturn(nomeArquivo);
        when(albumRepository.save(album)).thenReturn(album);
        when(minioService.obterUrlPreAssinada(nomeArquivo)).thenReturn("http://minio.com/capa-123.jpg");

        // Act
        AlbumResponse resultado = albumService.atualizarCapa(albumId, file);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(album.getCapaUrl()).isEqualTo(nomeArquivo);
        verify(minioService).enviarArquivo(file);
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar capa de álbum inexistente")
    void atualizarCapa_ComAlbumInexistente_LancaException() {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> albumService.atualizarCapa(albumId, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(minioService, never()).enviarArquivo(any());
    }

    @Test
    @DisplayName("Deve atualizar álbum com dados válidos")
    void atualizar_ComDadosValidos_AtualizaAlbum() {
        // Arrange
        AlbumRequest request = new AlbumRequest("Abbey Road Remastered", 1969, Arrays.asList(artistaId));
        Artista novoArtista = new Artista("Paul McCartney", "Rock", TipoArtista.SOLO);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistaRepository.findAllById(request.artistasIds())).thenReturn(Arrays.asList(novoArtista));
        when(albumRepository.save(album)).thenReturn(album);
        when(minioService.obterUrlPreAssinada(any())).thenReturn(null);

        // Act
        AlbumResponse resultado = albumService.atualizar(albumId, request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(album.getTitulo()).isEqualTo("Abbey Road Remastered");
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar álbum inexistente")
    void atualizar_ComAlbumInexistente_LancaException() {
        // Arrange
        AlbumRequest request = new AlbumRequest("Abbey Road", 1969, Arrays.asList(artistaId));
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> albumService.atualizar(albumId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar álbum existente")
    void desativar_ComAlbumExistente_DesativaAlbum() {
        // Arrange
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumRepository.save(album)).thenReturn(album);

        // Act
        albumService.desativar(albumId);

        // Assert
        assertThat(album.isAtivo()).isFalse();
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("Deve lançar exceção ao desativar álbum inexistente")
    void desativar_ComAlbumInexistente_LancaException() {
        // Arrange
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> albumService.desativar(albumId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Álbum não encontrado");

        verify(albumRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar URL assinada quando álbum tem capa")
    void toResponse_ComCapaUrl_RetornaUrlAssinada() {
        // Arrange
        album.setCapaUrl("capa-123.jpg");
        String urlAssinada = "http://minio.com/capa-123.jpg";

        when(albumRepository.buscarComFiltros(any(), any(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(album)));
        when(minioService.obterUrlPreAssinada("capa-123.jpg")).thenReturn(urlAssinada);

        // Act
        Page<AlbumResponse> resultado = albumService.listarTodos(PageRequest.of(0, 10), null, null);

        // Assert
        assertThat(resultado.getContent().get(0).capaUrl()).isEqualTo(urlAssinada);
        verify(minioService).obterUrlPreAssinada("capa-123.jpg");
    }

    @Test
    @DisplayName("Deve retornar null quando álbum não tem capa")
    void toResponse_SemCapaUrl_RetornaNulo() {
        // Arrange
        album.setCapaUrl(null);

        when(albumRepository.buscarComFiltros(any(), any(), any()))
                .thenReturn(new PageImpl<>(Arrays.asList(album)));

        // Act
        Page<AlbumResponse> resultado = albumService.listarTodos(PageRequest.of(0, 10), null, null);

        // Assert
        assertThat(resultado.getContent().get(0).capaUrl()).isNull();
        verify(minioService, never()).obterUrlPreAssinada(any());
    }
}
