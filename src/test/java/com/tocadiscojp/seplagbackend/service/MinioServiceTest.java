package com.tocadiscojp.seplagbackend.service;

import io.minio.*;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Testes do MinioService")
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "bucketName", BUCKET_NAME);
    }

    @Test
    @DisplayName("Deve enviar arquivo quando bucket existe")
    void enviarArquivo_ComBucketExistente_EnviaArquivo() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(file.getOriginalFilename()).thenReturn("test-image.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        String nomeArquivo = minioService.enviarArquivo(file);

        // Assert
        assertThat(nomeArquivo).isNotNull();
        assertThat(nomeArquivo).contains("test-image");
        assertThat(nomeArquivo).endsWith(".jpg");
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("Deve criar bucket e enviar arquivo quando bucket não existe")
    void enviarArquivo_SemBucket_CriaBucketEEnviaArquivo() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        // Act
        String nomeArquivo = minioService.enviarArquivo(file);

        // Assert
        assertThat(nomeArquivo).isNotNull();
        assertThat(nomeArquivo).endsWith(".png");
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Deve normalizar nome com acentos")
    void enviarArquivo_ComNomeComAcentos_NormalizaNome() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        when(file.getOriginalFilename()).thenReturn("imagem-com-açõés.jpg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        String nomeArquivo = minioService.enviarArquivo(file);

        // Assert
        assertThat(nomeArquivo).isNotNull();
        assertThat(nomeArquivo).contains("imagem-com-acoes");
        assertThat(nomeArquivo).doesNotContain("ç");
        assertThat(nomeArquivo).doesNotContain("õ");
        assertThat(nomeArquivo).doesNotContain("é");
    }

    @Test
    @DisplayName("Deve substituir caracteres especiais por underscore")
    void enviarArquivo_ComCaracteresEspeciais_SubstituiPorUnderscore() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        when(file.getOriginalFilename()).thenReturn("arquivo@com#caracteres$especiais!.pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getInputStream()).thenReturn(inputStream);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Act
        String nomeArquivo = minioService.enviarArquivo(file);

        // Assert
        assertThat(nomeArquivo).isNotNull();
        assertThat(nomeArquivo).contains("_");
        assertThat(nomeArquivo).doesNotContain("@");
        assertThat(nomeArquivo).doesNotContain("#");
        assertThat(nomeArquivo).doesNotContain("$");
        assertThat(nomeArquivo).doesNotContain("!");
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando ocorre erro no MinIO")
    void enviarArquivo_ComErroMinIO_LancaRuntimeException() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");

        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("MinIO error"));

        // Act & Assert
        assertThatThrownBy(() -> minioService.enviarArquivo(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao fazer o envio do arquivo para o MinIO");
    }

    @Test
    @DisplayName("Deve obter URL pré-assinada com nome válido")
    void obterUrlPreAssinada_ComNomeValido_RetornaUrl() throws Exception {
        // Arrange
        String nomeArquivo = "test-file.jpg";
        String urlEsperada = "http://minio.com/bucket/test-file.jpg?signature=abc123";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(urlEsperada);

        // Act
        String url = minioService.obterUrlPreAssinada(nomeArquivo);

        // Assert
        assertThat(url).isEqualTo(urlEsperada);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    @DisplayName("Deve retornar null quando nome do arquivo é nulo")
    void obterUrlPreAssinada_ComNomeNulo_RetornaNulo() throws Exception {
        // Act
        String url = minioService.obterUrlPreAssinada(null);

        // Assert
        assertThat(url).isNull();
        verify(minioClient, never()).getPresignedObjectUrl(any());
    }

    @Test
    @DisplayName("Deve retornar null quando nome do arquivo é vazio")
    void obterUrlPreAssinada_ComNomeVazio_RetornaNulo() throws Exception {
        // Act
        String url = minioService.obterUrlPreAssinada("");

        // Assert
        assertThat(url).isNull();
        verify(minioClient, never()).getPresignedObjectUrl(any());
    }

    @Test
    @DisplayName("Deve retornar null quando ocorre erro ao obter URL")
    void obterUrlPreAssinada_ComErro_RetornaNulo() throws Exception {
        // Arrange
        String nomeArquivo = "test-file.jpg";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("MinIO error"));

        // Act
        String url = minioService.obterUrlPreAssinada(nomeArquivo);

        // Assert
        assertThat(url).isNull();
    }
}
