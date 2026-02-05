package com.tocadiscojp.seplagbackend.controller;

import com.tocadiscojp.seplagbackend.model.Regional;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
import com.tocadiscojp.seplagbackend.service.RateLimitService;
import com.tocadiscojp.seplagbackend.service.RegionalService;
import com.tocadiscojp.seplagbackend.service.TokenService;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegionalController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do RegionalController")
class RegionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegionalService regionalService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    private Regional regional1;
    private Regional regional2;

    @BeforeEach
    void setUp() {
        regional1 = new Regional(1, "Regional Norte");
        regional1.setId(1L);
        regional2 = new Regional(2, "Regional Sul");
        regional2.setId(2L);

        // Configurar mock do RateLimitService para retornar um bucket válido
        Bucket mockBucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, Duration.ofMinutes(1)))
                .build();
        when(rateLimitService.resolveBucket(any())).thenReturn(mockBucket);
    }

    @Test
    @DisplayName("POST /v1/regionais/sincronizar - Deve sincronizar e retornar mensagem")
    void sincronizar_RetornaStatus200ComMensagem() throws Exception {
        // Arrange
        String mensagem = "Sincronização concluída. Criados: 2, Atualizados: 1, Inativados: 0";
        when(regionalService.sincronizarRegionais()).thenReturn(mensagem);

        // Act & Assert
        mockMvc.perform(post("/v1/regionais/sincronizar"))
                .andExpect(status().isOk())
                .andExpect(content().string(mensagem));

        verify(regionalService).sincronizarRegionais();
    }

    @Test
    @DisplayName("GET /v1/regionais - Deve listar todas as regionais")
    void listar_RetornaStatus200ComLista() throws Exception {
        // Arrange
        when(regionalService.listarTodas()).thenReturn(Arrays.asList(regional1, regional2));

        // Act & Assert
        mockMvc.perform(get("/v1/regionais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").value("Regional Norte"))
                .andExpect(jsonPath("$[1].nome").value("Regional Sul"));

        verify(regionalService).listarTodas();
    }
}
