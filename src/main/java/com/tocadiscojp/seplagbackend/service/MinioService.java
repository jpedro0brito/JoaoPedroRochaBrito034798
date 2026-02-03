package com.tocadiscojp.seplagbackend.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String enviarArquivo(MultipartFile file) {
        try {
            boolean existeBucket = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!existeBucket) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String originalNome = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalNome);

            String nameWithoutExtension = StringUtils.stripFilenameExtension(originalNome);
            String safeNome = normalizaNomeDoArquivo(nameWithoutExtension);

            String nomeDoArquivo = UUID.randomUUID() + "-" + safeNome + "." + extension;

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(nomeDoArquivo)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return nomeDoArquivo;

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao fazer o envio do arquivo para o MinIO", ex);
        }
    }

    public String obterUrlPreAssinada(String nomeDoArquivo) {
        if (nomeDoArquivo == null || nomeDoArquivo.isEmpty())
            return null;

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(nomeDoArquivo)
                            .expiry(30, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizaNomeDoArquivo(String originalNomeDoArquivo) {
        if (originalNomeDoArquivo == null)
            return "arquivo";

        String normalized = Normalizer.normalize(originalNomeDoArquivo, Normalizer.Form.NFD);

        String semAcentos = normalized.replaceAll("\\p{M}", "");

        return semAcentos.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }
}
