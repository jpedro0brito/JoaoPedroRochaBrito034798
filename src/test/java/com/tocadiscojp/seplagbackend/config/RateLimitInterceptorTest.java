package com.tocadiscojp.seplagbackend.config;

import com.tocadiscojp.seplagbackend.model.Usuario;
import com.tocadiscojp.seplagbackend.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RateLimitInterceptor")
class RateLimitInterceptorTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Bucket bucket;

    @Mock
    private ConsumptionProbe probe;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

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
    @DisplayName("Deve permitir requisição quando dentro do limite")
    void preHandle_DentroDoLimite_PermiteRequisicao() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(rateLimitService.resolveBucket("testuser")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(9L);

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        // Assert
        assertThat(result).isTrue();
        verify(response).addHeader("X-Rate-Limit-Remaining", "9");
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Deve bloquear requisição quando acima do limite")
    void preHandle_AcimaDoLimite_BloqueiaRequisicao() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(rateLimitService.resolveBucket("testuser")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(30_000_000_000L); // 30 segundos

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        // Assert
        assertThat(result).isFalse();
        verify(response).addHeader("X-Rate-Limit-Retry-After-Seconds", "30");
        verify(response).sendError(
                eq(HttpStatus.TOO_MANY_REQUESTS.value()),
                contains("limite de requisições")
        );
    }

    @Test
    @DisplayName("Deve permitir requisição quando usuário não está autenticado")
    void preHandle_SemUsuarioAutenticado_PermiteRequisicao() throws Exception {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        // Assert
        assertThat(result).isTrue();
        verify(rateLimitService, never()).resolveBucket(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Deve permitir requisição para usuário anônimo")
    void preHandle_UsuarioAnonimo_PermiteRequisicao() throws Exception {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        boolean result = rateLimitInterceptor.preHandle(request, response, new Object());

        // Assert
        assertThat(result).isTrue();
        verify(rateLimitService, never()).resolveBucket(anyString());
    }
}
