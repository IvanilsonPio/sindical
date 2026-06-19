package com.sindicato.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for file storage.
 * FILE STORAGE DISABLED: directory creation disabled for Railway deployment.
 * Re-enable when persistent storage is configured.
 */
@Configuration
public class StorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);

    // FILE_STORAGE_DISABLED: @PostConstruct init() removed to prevent startup failures
    // when no writable volume is available.

    public String getUploadDir() {
        return System.getProperty("java.io.tmpdir") + "/sindicato/uploads";
    }
}
