package com.keepy.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class GcsConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.sa-key-json}")
    private String saKeyJsonBase64;

    @Bean
    public Storage gcsClient() throws IOException {
        byte[] keyBytes = Base64.getDecoder().decode(saKeyJsonBase64);
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(keyBytes))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
