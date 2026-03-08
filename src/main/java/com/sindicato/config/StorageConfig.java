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
        try {
            // Create main upload directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            logger.info("Upload directory created/verified: {}", uploadPath);
            
            // Create receipts subdirectory
            Path recibosPath = uploadPath.resolve("recibos");
            Files.createDirectories(recibosPath);
            logger.info("Receipts directory created/verified: {}", recibosPath);
            
        } catch (IOException e) {
            logger.error("Could not create upload directories", e);
            throw new RuntimeException("Could not create upload directories", e);
        }
    }
    
    public String getUploadDir() {
        return uploadDir;
    }
}
