package com.tocadiscojp.seplagbackend.service;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do RateLimitService")
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
        // Injetar valor do rateLimit usando reflexão
        ReflectionTestUtils.setField(rateLimitService, "rateLimit", 10);
    }

    @Test
    @DisplayName("Deve criar novo bucket para login novo")
    void resolveBucket_ComNovoLogin_CriaNovoBucket() {
        // Arrange
        String login = "usuario1";

        // Act
        Bucket bucket = rateLimitService.resolveBucket(login);

        // Assert
        assertThat(bucket).isNotNull();
        assertThat(bucket.getAvailableTokens()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve retornar mesmo bucket para login existente")
    void resolveBucket_ComLoginExistente_RetornaMesmoBucket() {
        // Arrange
        String login = "usuario1";
        Bucket bucket1 = rateLimitService.resolveBucket(login);

        // Act
        Bucket bucket2 = rateLimitService.resolveBucket(login);

        // Assert
        assertThat(bucket1).isSameAs(bucket2);
    }

    @Test
    @DisplayName("Bucket deve ter capacidade de 10 tokens")
    void bucket_TemCapacidade10_Configurado() {
        // Arrange
        String login = "usuario1";

        // Act
        Bucket bucket = rateLimitService.resolveBucket(login);

        // Assert
        assertThat(bucket.getAvailableTokens()).isEqualTo(10);
    }

    @Test
    @DisplayName("Bucket deve consumir tokens corretamente")
    void bucket_ConsomeTokensCorretamente() {
        // Arrange
        String login = "usuario1";
        Bucket bucket = rateLimitService.resolveBucket(login);

        // Act
        boolean consumido1 = bucket.tryConsume(1);
        boolean consumido2 = bucket.tryConsume(5);

        // Assert
        assertThat(consumido1).isTrue();
        assertThat(consumido2).isTrue();
        assertThat(bucket.getAvailableTokens()).isEqualTo(4); // 10 - 1 - 5 = 4
    }

    @Test
    @DisplayName("Bucket deve rejeitar consumo quando não há tokens suficientes")
    void bucket_RejeitaConsumoQuandoSemTokens() {
        // Arrange
        String login = "usuario1";
        Bucket bucket = rateLimitService.resolveBucket(login);

        // Act
        bucket.tryConsume(10); // Consome todos os tokens
        boolean consumidoExtra = bucket.tryConsume(1);

        // Assert
        assertThat(consumidoExtra).isFalse();
        assertThat(bucket.getAvailableTokens()).isEqualTo(0);
    }

    @Test
    @DisplayName("Buckets diferentes para logins diferentes")
    void buckets_DiferentesParaLoginsDiferentes() {
        // Arrange
        String login1 = "usuario1";
        String login2 = "usuario2";

        // Act
        Bucket bucket1 = rateLimitService.resolveBucket(login1);
        Bucket bucket2 = rateLimitService.resolveBucket(login2);

        // Assert
        assertThat(bucket1).isNotSameAs(bucket2);
    }
}
