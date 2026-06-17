package com.sindicato.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for file storage.
 * Creates necessary directories for storing receipts and other files.
 */
@Configuration
public class StorageConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageConfig.class);
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        String[] dirs = { uploadDir, uploadDir + "/recibos", uploadDir + "/arquivos-gerais" };
        
        for (String dir : dirs) {
            Path path = Paths.get(dir).toAbsolutePath().normalize();
            try {
                Files.createDirectories(path);
                logger.info("Directory created/verified: {}", path);
            } catch (IOException e) {
                // Fallback para /tmp se não tiver permissão no diretório configurado
                logger.warn("Cannot create directory {}: {}. Falling back to /tmp.", path, e.getMessage());
                Path tmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "sindicato",
                        path.getFileName().toString()).toAbsolutePath().normalize();
                try {
                    Files.createDirectories(tmpPath);
                    logger.info("Using temp directory: {}", tmpPath);
                } catch (IOException ex2) {
                    logger.error("Could not create temp directory either: {}", tmpPath, ex2);
                }
            }
        }
    }
    
    public String getUploadDir() {
        return uploadDir;
    }
}
