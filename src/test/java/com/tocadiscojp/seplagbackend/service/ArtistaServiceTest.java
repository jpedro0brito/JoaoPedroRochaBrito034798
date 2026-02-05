package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.dto.ArtistaRequest;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.model.Album;
import com.tocadiscojp.seplagbackend.model.Artista;
import com.tocadiscojp.seplagbackend.repository.ArtistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ArtistaService")
class ArtistaServiceTest {

    @Mock
    private ArtistaRepository repository;

    @InjectMocks
    private ArtistaService artistaService;

    private Artista artista;
    private UUID artistaId;

    @BeforeEach
    void setUp() {
        artistaId = UUID.randomUUID();
        artista = new Artista("The Beatles", "Rock", TipoArtista.BANDA);
        artista.setId(artistaId);
    }

    @Test
    @DisplayName("Deve salvar artista com dados válidos")
    void salvar_ComDadosValidos_SalvaArtista() {
        // Arrange
        ArtistaRequest request = new ArtistaRequest("The Beatles", "Rock", TipoArtista.BANDA);
        when(repository.save(any(Artista.class))).thenReturn(artista);

        // Act
        ArtistaResponse resultado = artistaService.salvar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("The Beatles");
        assertThat(resultado.generoMusical()).isEqualTo("Rock");
        assertThat(resultado.tipo()).isEqualTo(TipoArtista.BANDA);
        verify(repository).save(any(Artista.class));
    }

    @Test
    @DisplayName("Deve alterar artista existente")
    void alterar_ComArtistaExistente_AtualizaArtista() {
        // Arrange
        ArtistaRequest request = new ArtistaRequest("The Beatles Updated", "Classic Rock", TipoArtista.BANDA);
        when(repository.findById(artistaId)).thenReturn(Optional.of(artista));
        when(repository.save(artista)).thenReturn(artista);

        // Act
        ArtistaResponse resultado = artistaService.alterar(artistaId, request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(artista.getNome()).isEqualTo("The Beatles Updated");
        assertThat(artista.getGeneroMusical()).isEqualTo("Classic Rock");
        verify(repository).save(artista);
    }

    @Test
    @DisplayName("Deve lançar exceção ao alterar artista inexistente")
    void alterar_ComArtistaInexistente_LancaException() {
        // Arrange
        ArtistaRequest request = new ArtistaRequest("The Beatles", "Rock", TipoArtista.BANDA);
        when(repository.findById(artistaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> artistaService.alterar(artistaId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Artista não encontrado");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar artistas com filtro de tipo")
    void listar_ComFiltroTipo_RetornaListaFiltrada() {
        // Arrange
        TipoArtista tipo = TipoArtista.BANDA;
        Sort sort = Sort.by(Sort.Direction.ASC, "nome");
        List<Artista> artistas = Arrays.asList(artista);

        when(repository.buscarComFiltros(tipo, null, sort)).thenReturn(artistas);

        // Act
        List<ArtistaResponse> resultado = artistaService.listar(tipo, null, "asc");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("The Beatles");
        verify(repository).buscarComFiltros(tipo, null, sort);
    }

    @Test
    @DisplayName("Deve listar artistas com ordenação descendente")
    void listar_ComOrdemDesc_RetornaOrdenadoDescendente() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.DESC, "nome");
        List<Artista> artistas = Arrays.asList(artista);

        when(repository.buscarComFiltros(null, null, sort)).thenReturn(artistas);

        // Act
        List<ArtistaResponse> resultado = artistaService.listar(null, null, "desc");

        // Assert
        assertThat(resultado).hasSize(1);
        verify(repository).buscarComFiltros(null, null, sort);
    }

    @Test
    @DisplayName("Deve listar artistas com ordenação ascendente por padrão")
    void listar_ComOrdemAsc_RetornaOrdenadoAscendente() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.ASC, "nome");
        List<Artista> artistas = Arrays.asList(artista);

        when(repository.buscarComFiltros(null, null, sort)).thenReturn(artistas);

        // Act
        List<ArtistaResponse> resultado = artistaService.listar(null, null, "asc");

        // Assert
        assertThat(resultado).hasSize(1);
        verify(repository).buscarComFiltros(null, null, sort);
    }

    @Test
    @DisplayName("Deve remover artista sem álbuns")
    void removerOuDesativar_ArtistaSemAlbuns_RemoveArtista() {
        // Arrange
        artista.setAlbuns(new ArrayList<>());
        when(repository.findById(artistaId)).thenReturn(Optional.of(artista));

        // Act
        artistaService.removerOuDesativar(artistaId);

        // Assert
        verify(repository).delete(artista);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar artista com álbuns e desativar seus álbuns")
    void removerOuDesativar_ArtistaComAlbuns_DesativaArtistaEAlbuns() {
        // Arrange
        Album album1 = new Album("Abbey Road", 1969);
        Album album2 = new Album("Let It Be", 1970);
        artista.setAlbuns(Arrays.asList(album1, album2));

        when(repository.findById(artistaId)).thenReturn(Optional.of(artista));
        when(repository.save(artista)).thenReturn(artista);

        // Act
        artistaService.removerOuDesativar(artistaId);

        // Assert
        assertThat(artista.isAtivo()).isFalse();
        assertThat(album1.isAtivo()).isFalse();
        assertThat(album2.isAtivo()).isFalse();
        verify(repository).save(artista);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao remover artista inexistente")
    void removerOuDesativar_ComArtistaInexistente_LancaException() {
        // Arrange
        when(repository.findById(artistaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> artistaService.removerOuDesativar(artistaId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Artista não encontrado");

        verify(repository, never()).delete(any());
        verify(repository, never()).save(any());
    }
}
