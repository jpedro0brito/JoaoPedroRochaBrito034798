package com.tocadiscojp.seplagbackend.config;

import com.tocadiscojp.seplagbackend.model.Usuario;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
import com.tocadiscojp.seplagbackend.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SecurityFilter")
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository repository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setLogin("testuser");
        usuario.setSenha("password");
        usuario.setRole("USER");
    }

    @Test
    @DisplayName("Deve autenticar usuário com token válido")
    void doFilterInternal_ComTokenValido_AutenticaUsuario() throws Exception {
        // Arrange
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.getSubject(token)).thenReturn("testuser");
        when(repository.findByLogin("testuser")).thenReturn(usuario);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(usuario);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando não há token")
    void doFilterInternal_SemToken_NaoAutenticaUsuario() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(tokenService, never()).getSubject(anyString());
    }

    @Test
    @DisplayName("Não deve autenticar com token inválido")
    void doFilterInternal_ComTokenInvalido_NaoAutenticaUsuario() throws Exception {
        // Arrange
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.getSubject(token)).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(repository, never()).findByLogin(anyString());
    }

    @Test
    @DisplayName("Deve extrair token do header Authorization")
    void recuperarToken_ComHeaderValido_RetornaToken() throws Exception {
        // Arrange
        String token = "my-jwt-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenService.getSubject(token)).thenReturn("testuser");
        when(repository.findByLogin("testuser")).thenReturn(usuario);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenService).getSubject(token);
    }

    @Test
    @DisplayName("Deve retornar null quando header Authorization não existe")
    void recuperarToken_SemHeader_RetornaNulo() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenService, never()).getSubject(anyString());
    }
}
