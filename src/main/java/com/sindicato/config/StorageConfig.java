package com.sindicato.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for file storage.
 */
@Configuration
public class StorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);

    @Value("${file.upload-dir:${java.io.tmpdir}/sindicato/uploads}")
    private String uploadDir;

    // Diretórios criados pelo docker-entrypoint.sh antes do Java iniciar — sem @PostConstruct

    public String getUploadDir() {
        return uploadDir;
    }
}
