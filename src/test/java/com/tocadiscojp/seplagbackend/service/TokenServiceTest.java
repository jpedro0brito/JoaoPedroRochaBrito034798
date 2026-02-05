package com.tocadiscojp.seplagbackend.service;

import com.tocadiscojp.seplagbackend.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do TokenService")
class TokenServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-token-generation-minimum-256-bits";
    
    private TokenService tokenService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Injetar o secret via ReflectionTestUtils
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setLogin("testuser");
        usuario.setSenha("password");
    }

    @Test
    @DisplayName("Deve gerar token com expiração de 5 minutos")
    void gerarToken_ComUsuarioValido_GeraTokenComExpiracao5Minutos() {
        // Act
        String token = tokenService.gerarToken(usuario);

        // Assert
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // Verificar claims do token
        var claims = Jwts.parserBuilder()
                .setSigningKey(getChave())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.getIssuer()).isEqualTo("API Seplag");
        assertThat(claims.get("id")).isNotNull();

        // Verificar expiração (aproximadamente 5 minutos)
        long expiracao = claims.getExpiration().getTime() - System.currentTimeMillis();
        assertThat(expiracao).isBetween(4 * 60 * 1000L, 5 * 60 * 1000L);
    }

    @Test
    @DisplayName("Deve gerar refresh token com expiração de 30 minutos")
    void gerarRefreshToken_ComUsuarioValido_GeraTokenComExpiracao30Minutos() {
        // Act
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        // Assert
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();

        // Verificar claims do token
        var claims = Jwts.parserBuilder()
                .setSigningKey(getChave())
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("testuser");

        // Verificar expiração (aproximadamente 30 minutos)
        long expiracao = claims.getExpiration().getTime() - System.currentTimeMillis();
        assertThat(expiracao).isBetween(29 * 60 * 1000L, 30 * 60 * 1000L);
    }

    @Test
    @DisplayName("Deve retornar subject de token válido")
    void getSubject_ComTokenValido_RetornaUsername() {
        // Arrange
        String token = tokenService.gerarToken(usuario);

        // Act
        String subject = tokenService.getSubject(token);

        // Assert
        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Deve retornar null para token inválido")
    void getSubject_ComTokenInvalido_RetornaNulo() {
        // Arrange
        String tokenInvalido = "token.invalido.aqui";

        // Act
        String subject = tokenService.getSubject(tokenInvalido);

        // Assert
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Deve retornar null para token expirado")
    void getSubject_ComTokenExpirado_RetornaNulo() {
        // Arrange
        Date expirationDate = new Date(System.currentTimeMillis() - 1000); // Expirado há 1 segundo

        String tokenExpirado = Jwts.builder()
                .setIssuer("API Seplag")
                .setSubject(usuario.getUsername())
                .claim("id", usuario.getId())
                .setExpiration(expirationDate)
                .signWith(getChave())
                .compact();

        // Act
        String subject = tokenService.getSubject(tokenExpirado);

        // Assert
        assertThat(subject).isNull();
    }

    @Test
    @DisplayName("Token deve conter claims corretos (issuer e id)")
    void token_ContemClaimsCorretos_IssuerEId() {
        // Act
        String token = tokenService.gerarToken(usuario);

        // Assert
        var claims = Jwts.parserBuilder()
                .setSigningKey(getChave())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getIssuer()).isEqualTo("API Seplag");
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("id").toString()).isEqualTo(usuario.getId().toString());
    }

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
