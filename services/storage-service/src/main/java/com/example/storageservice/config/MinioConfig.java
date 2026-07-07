package com.example.storageservice.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    07/07/2026 at 9:26
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Configuration
public class MinioConfig {
    @Bean
    MinioClient minioClient(StorageProperties p) {
        return MinioClient.builder().endpoint(p.minio().endpoint()).credentials(p.minio().accessKey(), p.minio().secretKey()).build();
    }
}