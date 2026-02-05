package com.tocadiscojp.seplagbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tocadiscojp.seplagbackend.dto.ArtistaRequest;
import com.tocadiscojp.seplagbackend.dto.ArtistaResponse;
import com.tocadiscojp.seplagbackend.enums.TipoArtista;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
import com.tocadiscojp.seplagbackend.service.ArtistaService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistaController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do ArtistaController")
class ArtistaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArtistaService artistaService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private UUID artistaId;
    private ArtistaResponse artistaResponse;

    @BeforeEach
    void setUp() {
        artistaId = UUID.randomUUID();
        artistaResponse = new ArtistaResponse(artistaId, "The Beatles", "Rock", TipoArtista.BANDA);

        // Configurar mock do RateLimitService para retornar um bucket válido
        Bucket mockBucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, Duration.ofMinutes(1)))
                .build();
        when(rateLimitService.resolveBucket(any())).thenReturn(mockBucket);
    }

    @Test
    @DisplayName("GET /v1/artistas - Deve listar artistas com filtros")
    void listar_ComFiltros_RetornaStatus200() throws Exception {
        // Arrange
        ArtistaResponse artista2 = new ArtistaResponse(UUID.randomUUID(), "Pink Floyd", "Rock", TipoArtista.BANDA);
        when(artistaService.listar(eq(TipoArtista.BANDA), isNull(), eq("asc")))
                .thenReturn(Arrays.asList(artistaResponse, artista2));

        // Act & Assert
        mockMvc.perform(get("/v1/artistas")
                        .param("tipo", "BANDA")
                        .param("ordem", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").value("The Beatles"))
                .andExpect(jsonPath("$[1].nome").value("Pink Floyd"));

        verify(artistaService).listar(eq(TipoArtista.BANDA), isNull(), eq("asc"));
    }

    @Test
    @DisplayName("GET /v1/artistas - Deve listar artistas sem filtros")
    void listar_SemFiltros_RetornaTodosArtistas() throws Exception {
        // Arrange
        when(artistaService.listar(isNull(), isNull(), eq("asc")))
                .thenReturn(Collections.singletonList(artistaResponse));

        // Act & Assert
        mockMvc.perform(get("/v1/artistas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").value("The Beatles"));
    }

    @Test
    @DisplayName("POST /v1/artistas - Deve cadastrar artista com dados válidos")
    void cadastrar_ComDadosValidos_RetornaStatus200() throws Exception {
        // Arrange
        ArtistaRequest request = new ArtistaRequest("The Beatles", "Rock", TipoArtista.BANDA);
        when(artistaService.salvar(any(ArtistaRequest.class))).thenReturn(artistaResponse);

        // Act & Assert
        mockMvc.perform(post("/v1/artistas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("The Beatles"))
                .andExpect(jsonPath("$.generoMusical").value("Rock"))
                .andExpect(jsonPath("$.tipo").value("BANDA"));

        verify(artistaService).salvar(any(ArtistaRequest.class));
    }

    @Test
    @DisplayName("PUT /v1/artistas/{id} - Deve atualizar artista com dados válidos")
    void atualizar_ComDadosValidos_RetornaStatus200() throws Exception {
        // Arrange
        ArtistaRequest request = new ArtistaRequest("The Beatles Updated", "Classic Rock", TipoArtista.BANDA);
        ArtistaResponse updatedResponse = new ArtistaResponse(artistaId, "The Beatles Updated", "Classic Rock", TipoArtista.BANDA);
        when(artistaService.alterar(eq(artistaId), any(ArtistaRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/v1/artistas/{id}", artistaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("The Beatles Updated"))
                .andExpect(jsonPath("$.generoMusical").value("Classic Rock"));

        verify(artistaService).alterar(eq(artistaId), any(ArtistaRequest.class));
    }

    @Test
    @DisplayName("DELETE /v1/artistas/{id} - Deve remover artista existente")
    void remover_ComArtistaExistente_RetornaStatus204() throws Exception {
        // Arrange
        doNothing().when(artistaService).removerOuDesativar(artistaId);

        // Act & Assert
        mockMvc.perform(delete("/v1/artistas/{id}", artistaId))
                .andExpect(status().isNoContent());

        verify(artistaService).removerOuDesativar(artistaId);
    }
}
