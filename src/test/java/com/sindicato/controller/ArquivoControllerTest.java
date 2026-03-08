package com.sindicato.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sindicato.exception.BusinessException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Arquivo;
import com.sindicato.model.Socio;
import com.sindicato.service.ArquivoService;

/**
 * Unit tests for ArquivoController.
 * Tests REST endpoints for file management operations.
 */
@WebMvcTest(ArquivoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArquivoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ArquivoService arquivoService;
    
    private Socio socio;
    private Arquivo arquivo;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        socio = new Socio();
        socio.setId(1L);
        socio.setNome("João Silva");
        socio.setCpf("123.456.789-00");
        
        arquivo = new Arquivo(
            socio,
            "documento.pdf",
            "1234567890_abc123_documento.pdf",
            "application/pdf",
            1024L,
            "/uploads/socios/1/1234567890_abc123_documento.pdf"
        );
        arquivo.setId(1L);
        arquivo.setCriadoEm(LocalDateTime.now());
    }
    
    @Test
    void uploadArquivosShouldReturnCreatedStatus() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "files",
            "test1.pdf",
            "application/pdf",
            "test content 1".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
            "files",
            "test2.pdf",
            "application/pdf",
            "test content 2".getBytes()
        );
        
        Arquivo arquivo2 = new Arquivo(
            socio,
            "test2.pdf",
            "1234567890_def456_test2.pdf",
            "application/pdf",
            2048L,
            "/uploads/socios/1/1234567890_def456_test2.pdf"
        );
        arquivo2.setId(2L);
        arquivo2.setCriadoEm(LocalDateTime.now());
        
        List<Arquivo> arquivos = Arrays.asList(arquivo, arquivo2);
        when(arquivoService.uploadArquivos(eq(1L), any())).thenReturn(arquivos);
        
        // Act & Assert
        mockMvc.perform(multipart("/api/arquivos/upload/1")
                .file(file1)
                .file(file2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nomeOriginal").value("documento.pdf"))
                .andExpect(jsonPath("$[0].socioNome").value("João Silva"))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(arquivoService).uploadArquivos(eq(1L), any());
    }
    
    @Test
    void uploadArquivosShouldReturnBadRequestWhenNoFiles() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "files",
            "",
            "application/pdf",
            new byte[0]
        );
        
        when(arquivoService.uploadArquivos(eq(1L), any()))
            .thenThrow(new BusinessException("NO_FILES_PROVIDED", "Nenhum arquivo foi enviado"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/arquivos/upload/1")
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NO_FILES_PROVIDED"));
    }
    
    @Test
    void uploadArquivosShouldReturnBadRequestWhenFileTooLarge() throws Exception {
        // Arrange
        MockMultipartFile largeFile = new MockMultipartFile(
            "files",
            "large.pdf",
            "application/pdf",
            new byte[1024 * 1024 * 11] // 11MB
        );
        
        when(arquivoService.uploadArquivos(eq(1L), any()))
            .thenThrow(new BusinessException("FILE_TOO_LARGE", "Arquivo excede tamanho máximo permitido"));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/arquivos/upload/1")
                .file(largeFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_TOO_LARGE"));
    }
    
    @Test
    void uploadArquivosShouldReturnNotFoundWhenSocioDoesNotExist() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "files",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );
        
        when(arquivoService.uploadArquivos(eq(999L), any()))
            .thenThrow(new ResourceNotFoundException("Socio", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(multipart("/api/arquivos/upload/999")
                .file(file))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void listarArquivosShouldReturnListOfFiles() throws Exception {
        // Arrange
        Arquivo arquivo2 = new Arquivo(
            socio,
            "foto.jpg",
            "1234567890_xyz789_foto.jpg",
            "image/jpeg",
            2048L,
            "/uploads/socios/1/1234567890_xyz789_foto.jpg"
        );
        arquivo2.setId(2L);
        arquivo2.setCriadoEm(LocalDateTime.now());
        
        List<Arquivo> arquivos = Arrays.asList(arquivo, arquivo2);
        when(arquivoService.listarArquivos(1L)).thenReturn(arquivos);
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/socio/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nomeOriginal").value("documento.pdf"))
                .andExpect(jsonPath("$[0].tipoConteudo").value("application/pdf"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nomeOriginal").value("foto.jpg"));
        
        verify(arquivoService).listarArquivos(1L);
    }
    
    @Test
    void listarArquivosShouldReturnEmptyListWhenNoFiles() throws Exception {
        // Arrange
        when(arquivoService.listarArquivos(1L)).thenReturn(Arrays.asList());
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/socio/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(arquivoService).listarArquivos(1L);
    }
    
    @Test
    void listarArquivosShouldReturnNotFoundWhenSocioDoesNotExist() throws Exception {
        // Arrange
        when(arquivoService.listarArquivos(999L))
            .thenThrow(new ResourceNotFoundException("Socio", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/socio/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void downloadArquivoShouldReturnFileWithCorrectHeaders() throws Exception {
        // Arrange
        byte[] fileContent = "test file content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);
        
        when(arquivoService.buscarArquivo(1L)).thenReturn(arquivo);
        when(arquivoService.downloadArquivo(1L)).thenReturn(resource);
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"documento.pdf\""))
                .andExpect(header().string("Content-Length", "1024"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(content().bytes(fileContent));
        
        verify(arquivoService).buscarArquivo(1L);
        verify(arquivoService).downloadArquivo(1L);
    }
    
    @Test
    void downloadArquivoShouldReturnNotFoundWhenFileDoesNotExist() throws Exception {
        // Arrange
        when(arquivoService.buscarArquivo(999L))
            .thenThrow(new ResourceNotFoundException("Arquivo", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/999/download"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void downloadArquivoShouldReturnBadRequestWhenFileNotFoundInStorage() throws Exception {
        // Arrange
        when(arquivoService.buscarArquivo(1L)).thenReturn(arquivo);
        when(arquivoService.downloadArquivo(1L))
            .thenThrow(new BusinessException("FILE_NOT_FOUND_IN_STORAGE", "Arquivo não encontrado no sistema de arquivos"));
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/1/download"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("FILE_NOT_FOUND_IN_STORAGE"));
    }
    
    @Test
    void excluirArquivoShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(arquivoService).excluirArquivo(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/arquivos/1"))
                .andExpect(status().isNoContent());
        
        verify(arquivoService).excluirArquivo(1L);
    }
    
    @Test
    void excluirArquivoShouldReturnNotFoundWhenFileDoesNotExist() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Arquivo", "id", 999L))
            .when(arquivoService).excluirArquivo(999L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/arquivos/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void obterEstatisticasShouldReturnStorageStatistics() throws Exception {
        // Arrange
        ArquivoService.ArquivoStatistics stats = new ArquivoService.ArquivoStatistics(
            5L,
            5242880L, // 5 MB
            104857600L, // 100 MB available
            104857600L + 5242880L // 105 MB total
        );
        
        when(arquivoService.obterEstatisticas(1L)).thenReturn(stats);
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/socio/1/estatisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeArquivos").value(5))
                .andExpect(jsonPath("$.tamanhoTotal").value(5242880))
                .andExpect(jsonPath("$.espacoDisponivel").value(104857600))
                .andExpect(jsonPath("$.percentualUtilizado").exists());
        
        verify(arquivoService).obterEstatisticas(1L);
    }
    
    @Test
    void obterEstatisticasShouldReturnNotFoundWhenSocioDoesNotExist() throws Exception {
        // Arrange
        when(arquivoService.obterEstatisticas(999L))
            .thenThrow(new ResourceNotFoundException("Socio", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(get("/api/arquivos/socio/999/estatisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
