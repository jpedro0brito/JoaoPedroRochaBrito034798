package com.tocadiscojp.seplagbackend.config;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;

    public MinioHealthIndicator(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public Health health() {
        try {
            minioClient.bucketExists(BucketExistsArgs.builder().bucket("health-check").build());

            return Health.up()
                    .withDetail("sistema", "MinIO Storage")
                    .withDetail("status", "Conectado e Respondendo")
                    .build();

        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("sistema", "MinIO Storage")
                    .withDetail("erro", "Não foi possível conectar ao MinIO")
                    .build();
        }
    }
}