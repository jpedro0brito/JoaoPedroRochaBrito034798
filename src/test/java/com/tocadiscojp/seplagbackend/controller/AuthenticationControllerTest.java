package com.tocadiscojp.seplagbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tocadiscojp.seplagbackend.dto.LoginRequest;
import com.tocadiscojp.seplagbackend.dto.RefreshTokenRequest;
import com.tocadiscojp.seplagbackend.model.Usuario;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes do AuthenticationController")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private RateLimitService rateLimitService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setLogin("testuser");
        usuario.setSenha("password");
        usuario.setRole("USER");

        // Configurar mock do RateLimitService para retornar um bucket válido
        Bucket mockBucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(100).refillGreedy(100, Duration.ofMinutes(1)))
                .build();
        when(rateLimitService.resolveBucket(any())).thenReturn(mockBucket);
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar tokens com credenciais válidas")
    void login_ComCredenciaisValidas_RetornaTokens() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password");
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                usuario.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.gerarToken(usuario)).thenReturn("access-token-123");
        when(tokenService.gerarRefreshToken(usuario)).thenReturn("refresh-token-456");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService).gerarToken(usuario);
        verify(tokenService).gerarRefreshToken(usuario);
    }

    @Test
    @DisplayName("POST /auth/refresh - Deve retornar novos tokens com refresh token válido")
    void refreshToken_ComTokenValido_RetornaNovoToken() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(tokenService.getSubject("valid-refresh-token")).thenReturn("testuser");
        when(usuarioRepository.findByLogin("testuser")).thenReturn(usuario);
        when(tokenService.gerarToken(usuario)).thenReturn("new-access-token");
        when(tokenService.gerarRefreshToken(usuario)).thenReturn("new-refresh-token");

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(tokenService).getSubject("valid-refresh-token");
        verify(usuarioRepository).findByLogin("testuser");
    }

    @Test
    @DisplayName("POST /auth/refresh - Deve retornar 403 com token inválido")
    void refreshToken_ComTokenInvalido_RetornaStatus403() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
        when(tokenService.getSubject("invalid-token")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(tokenService).getSubject("invalid-token");
        verify(usuarioRepository, never()).findByLogin(anyString());
    }

    @Test
    @DisplayName("POST /auth/refresh - Deve retornar 403 quando usuário não existe")
    void refreshToken_ComUsuarioInexistente_RetornaStatus403() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("valid-token");
        when(tokenService.getSubject("valid-token")).thenReturn("nonexistent");
        when(usuarioRepository.findByLogin("nonexistent")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(usuarioRepository).findByLogin("nonexistent");
        verify(tokenService, never()).gerarToken(any());
    }
}
