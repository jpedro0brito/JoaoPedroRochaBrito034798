package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.model.Usuario;
import com.tocadiscojp.seplagbackend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AuthorizationService")
class AuthorizationServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setLogin("testuser");
        usuario.setSenha("password");
    }

    @Test
    @DisplayName("Deve carregar usuário por username quando existe")
    void loadUserByUsername_ComUsuarioExistente_RetornaUsuario() {
        // Arrange
        String username = "testuser";
        when(repository.findByLogin(username)).thenReturn(usuario);

        // Act
        UserDetails resultado = authorizationService.loadUserByUsername(username);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("testuser");
        verify(repository).findByLogin(username);
    }

    @Test
    @DisplayName("Deve retornar null quando usuário não existe")
    void loadUserByUsername_ComUsuarioInexistente_RetornaNulo() {
        // Arrange
        String username = "inexistente";
        when(repository.findByLogin(username)).thenReturn(null);

        // Act
        UserDetails resultado = authorizationService.loadUserByUsername(username);

        // Assert
        assertThat(resultado).isNull();
        verify(repository).findByLogin(username);
    }
}
