package com.sindicato.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sindicato.dto.ArquivoResponse;
import com.sindicato.exception.BusinessException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Arquivo;
import com.sindicato.service.ArquivoService;

/**
 * REST controller for file management operations.
 * Provides endpoints for multiple file upload, listing, download, and deletion.
 * 
 * Requirements: 5.3, 5.4, 5.5
 */
@RestController
@RequestMapping("/api/arquivos")
public class ArquivoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ArquivoController.class);
    
    private final ArquivoService arquivoService;
    
    public ArquivoController(ArquivoService arquivoService) {
        this.arquivoService = arquivoService;
    }
    
    /**
     * Uploads multiple files for a specific socio.
     * Validates each file before storing and persists metadata in the database.
     * 
     * Requirement 5.1: Associate file to correct socio and validate file format
     * Requirement 5.2: Verify maximum allowed size and accepted file types
     * 
     * @param socioId ID of the socio who owns the files
     * @param files Array of files to upload
     * @return List of ArquivoResponse with HTTP 201 status
     */
    @PostMapping("/upload/{socioId}")
    public ResponseEntity<List<ArquivoResponse>> uploadArquivos(
            @PathVariable Long socioId,
            @RequestParam("files") MultipartFile[] files) {
        
        logger.info("POST /api/arquivos/upload/{} - Uploading {} file(s)", socioId, files.length);
        
        List<Arquivo> arquivos = arquivoService.uploadArquivos(socioId, files);
        List<ArquivoResponse> response = arquivos.stream()
                .map(ArquivoResponse::new)
                .collect(Collectors.toList());
        
        logger.info("Successfully uploaded {} file(s) for socio {}", response.size(), socioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Lists all files for a specific socio.
     * Returns files with upload date and original name.
     * 
     * Requirement 5.3: List all documents with upload date and original name
     * 
     * @param socioId ID of the socio
     * @return List of ArquivoResponse
     */
    @GetMapping("/socio/{socioId}")
    public ResponseEntity<List<ArquivoResponse>> listarArquivos(@PathVariable Long socioId) {
        logger.debug("GET /api/arquivos/socio/{} - Listing files", socioId);
        
        List<Arquivo> arquivos = arquivoService.listarArquivos(socioId);
        List<ArquivoResponse> response = arquivos.stream()
                .map(ArquivoResponse::new)
                .collect(Collectors.toList());
        
        logger.debug("Found {} file(s) for socio {}", response.size(), socioId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Downloads a specific file.
     * Streams large files efficiently using Spring's Resource abstraction.
     * 
     * Requirement 5.4: Allow download of previously uploaded files
     * 
     * @param id File ID
     * @return File as Resource with appropriate headers for streaming
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long id) {
        logger.info("GET /api/arquivos/{}/download - Downloading file", id);
        
        Arquivo arquivo = arquivoService.buscarArquivo(id);
        Resource resource = arquivoService.downloadArquivo(id);
        
        // Set headers for file download with streaming support
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(arquivo.getTipoConteudo()));
        headers.setContentDispositionFormData("attachment", arquivo.getNomeOriginal());
        headers.setContentLength(arquivo.getTamanho());
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);
        
        logger.info("File {} downloaded successfully", arquivo.getNomeOriginal());
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
    
    /**
     * Deletes a file from the system.
     * Removes both the physical file and the database record.
     * Maintains operation record for audit purposes.
     * 
     * Requirement 5.5: Remove file from storage and maintain operation record
     * 
     * @param id File ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirArquivo(@PathVariable Long id) {
        logger.info("DELETE /api/arquivos/{} - Deleting file", id);
        
        arquivoService.excluirArquivo(id);
        
        logger.info("File {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Gets storage statistics for a specific socio.
     * Returns file count, total size, available space, and usage percentage.
     * 
     * @param socioId ID of the socio
     * @return Map with storage statistics
     */
    @GetMapping("/socio/{socioId}/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas(@PathVariable Long socioId) {
        logger.debug("GET /api/arquivos/socio/{}/estatisticas - Getting storage statistics", socioId);
        
        ArquivoService.ArquivoStatistics stats = arquivoService.obterEstatisticas(socioId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("quantidadeArquivos", stats.getQuantidadeArquivos());
        response.put("tamanhoTotal", stats.getTamanhoTotal());
        response.put("tamanhoTotalFormatado", stats.getTamanhoTotalFormatado());
        response.put("espacoDisponivel", stats.getEspacoDisponivel());
        response.put("espacoDisponivelFormatado", stats.getEspacoDisponivelFormatado());
        response.put("limiteTotal", stats.getLimiteTotal());
        response.put("limiteTotalFormatado", stats.getLimiteTotalFormatado());
        response.put("percentualUtilizado", stats.getPercentualUtilizado());
        
        return ResponseEntity.ok(response);
    }
    
    // Exception Handlers
    
    /**
     * Handles ResourceNotFoundException.
     * 
     * @param ex ResourceNotFoundException
     * @return Error response with HTTP 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("BUS001", ex.getMessage());
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles BusinessException.
     * 
     * @param ex BusinessException
     * @return Error response with HTTP 400 status
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
        
        logger.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles generic exceptions.
     * 
     * @param ex Exception
     * @return Error response with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse("SYS003", "Erro interno do servidor");
        
        logger.error("Internal server error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * DTO for error responses.
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
