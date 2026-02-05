package com.tocadiscojp.seplagbackend.config;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MinioHealthIndicator")
class MinioHealthIndicatorTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioHealthIndicator minioHealthIndicator;

    @Test
    @DisplayName("Deve retornar UP quando MinIO está disponível")
    void health_ComMinIODisponivel_RetornaUp() throws Exception {
        // Arrange
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        Health health = minioHealthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("sistema", "MinIO Storage");
        assertThat(health.getDetails()).containsEntry("status", "Conectado e Respondendo");
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
    }

    @Test
    @DisplayName("Deve retornar DOWN quando MinIO está indisponível")
    void health_ComMinIOIndisponivel_RetornaDown() throws Exception {
        // Arrange
        Exception exception = new RuntimeException("Connection refused");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(exception);

        // Act
        Health health = minioHealthIndicator.health();

        // Assert
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("sistema", "MinIO Storage");
        assertThat(health.getDetails()).containsEntry("erro", "Não foi possível conectar ao MinIO");
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
    }
}
