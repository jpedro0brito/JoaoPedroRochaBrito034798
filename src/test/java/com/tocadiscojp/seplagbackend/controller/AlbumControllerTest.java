package com.tocadiscojp.seplagbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tocadiscojp.seplagbackend.dto.AlbumRequest;
import com.tocadiscojp.seplagbackend.dto.AlbumResponse;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
import com.tocadiscojp.seplagbackend.service.AlbumService;
import com.tocadiscojp.seplagbackend.service.RateLimitService;
import com.tocadiscojp.seplagbackend.service.TokenService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlbumController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do AlbumController")
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlbumService albumService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private UUID albumId;
    private UUID artistaId;
    private AlbumResponse albumResponse;

    @BeforeEach
    void setUp() {
        albumId = UUID.randomUUID();
        artistaId = UUID.randomUUID();
        ArtistaResponse artistaResponse = new ArtistaResponse(artistaId, "The Beatles", "Rock", TipoArtista.BANDA);
        albumResponse = new AlbumResponse(albumId, "Abbey Road", 1969, null, Collections.singletonList(artistaResponse));

        // Configurar mock do RateLimitService para retornar um bucket válido
        Bucket mockBucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, Duration.ofMinutes(1)))
                .build();
        when(rateLimitService.resolveBucket(any())).thenReturn(mockBucket);
    }

    @Test
    @DisplayName("GET /v1/albuns - Deve listar álbuns com paginação")
    void listar_ComPaginacao_RetornaStatus200() throws Exception {
        // Arrange
        PageImpl<AlbumResponse> page = new PageImpl<>(Collections.singletonList(albumResponse), PageRequest.of(0, 10), 1);
        when(albumService.listarTodos(any(), any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/albuns")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].titulo").value("Abbey Road"))
                .andExpect(jsonPath("$.content[0].anoLancamento").value(1969));

        verify(albumService).listarTodos(any(), any(), any());
    }

    @Test
    @DisplayName("GET /v1/albuns - Deve listar álbuns com filtros")
    void listar_ComFiltros_RetornaAlbunsFiltrados() throws Exception {
        // Arrange
        PageImpl<AlbumResponse> page = new PageImpl<>(Collections.singletonList(albumResponse));
        when(albumService.listarTodos(any(), eq(TipoArtista.BANDA), eq("Beatles"))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/albuns")
                        .param("tipoArtista", "BANDA")
                        .param("nomeArtista", "Beatles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Abbey Road"));

        verify(albumService).listarTodos(any(), eq(TipoArtista.BANDA), eq("Beatles"));
    }

    @Test
    @DisplayName("POST /v1/albuns - Deve cadastrar álbum com dados válidos")
    void cadastrar_ComDadosValidos_RetornaStatus201() throws Exception {
        // Arrange
        AlbumRequest request = new AlbumRequest("Abbey Road", 1969, Collections.singletonList(artistaId));
        when(albumService.cadastrar(any(AlbumRequest.class))).thenReturn(albumResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/albuns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Abbey Road"))
                .andExpect(jsonPath("$.anoLancamento").value(1969));

        verify(albumService).cadastrar(any(AlbumRequest.class));
    }

    @Test
    @DisplayName("POST /v1/albuns/{id}/capa - Deve fazer upload de capa")
    void uploadCapa_ComArquivoValido_RetornaStatus200() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "capa.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        AlbumResponse responseWithCapa = new AlbumResponse(albumId, "Abbey Road", 1969, "http://minio.com/capa.jpg",
                Collections.singletonList(new ArtistaResponse(artistaId, "The Beatles", "Rock", TipoArtista.BANDA)));
        when(albumService.atualizarCapa(eq(albumId), any())).thenReturn(responseWithCapa);

        // Act & Assert
        mockMvc.perform(multipart("/v1/albuns/{id}/capa", albumId)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capaUrl").value("http://minio.com/capa.jpg"));

        verify(albumService).atualizarCapa(eq(albumId), any());
    }

    @Test
    @DisplayName("PUT /v1/albuns/{id} - Deve atualizar álbum com dados válidos")
    void atualizar_ComDadosValidos_RetornaStatus200() throws Exception {
        // Arrange
        AlbumRequest request = new AlbumRequest("Abbey Road Remastered", 1969, Collections.singletonList(artistaId));
        AlbumResponse updatedResponse = new AlbumResponse(albumId, "Abbey Road Remastered", 1969, null,
                Collections.singletonList(new ArtistaResponse(artistaId, "The Beatles", "Rock", TipoArtista.BANDA)));
        when(albumService.atualizar(eq(albumId), any(AlbumRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/v1/albuns/{id}", albumId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Abbey Road Remastered"));

        verify(albumService).atualizar(eq(albumId), any(AlbumRequest.class));
    }

    @Test
    @DisplayName("DELETE /v1/albuns/{id} - Deve desativar álbum existente")
    void desativar_ComAlbumExistente_RetornaStatus204() throws Exception {
        // Arrange
        doNothing().when(albumService).desativar(albumId);

        // Act & Assert
        mockMvc.perform(delete("/v1/albuns/{id}", albumId))
                .andExpect(status().isNoContent());

        verify(albumService).desativar(albumId);
    }
}
