package com.sindicato.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.sindicato.exception.BusinessException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Arquivo;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.ArquivoRepository;
import com.sindicato.repository.SocioRepository;
import com.sindicato.util.ArquivoConstants;

/**
 * Testes unitários para ArquivoService.
 * Valida upload, download, exclusão e validações de arquivos.
 */
@ExtendWith(MockitoExtension.class)
class ArquivoServiceTest {
    
    @Mock
    private ArquivoRepository arquivoRepository;
    
    @Mock
    private SocioRepository socioRepository;
    
    @Mock
    private AuditService auditService;
    
    private ArquivoService arquivoService;
    
    @TempDir
    Path tempDir;
    
    private Socio socioTeste;
    private Arquivo arquivoTeste;
    
    @BeforeEach
    void setUp() {
        // Cria o serviço manualmente com o diretório temporário
        arquivoService = new ArquivoService(arquivoRepository, socioRepository, auditService, tempDir.toString());
        
        // Cria sócio de teste
        socioTeste = new Socio("João Silva", "123.456.789-00", "MAT001");
        socioTeste.setId(1L);
        socioTeste.setStatus(StatusSocio.ATIVO);
        
        // Cria arquivo de teste
        arquivoTeste = new Arquivo(
            socioTeste,
            "documento.pdf",
            "123456_abc_documento.pdf",
            "application/pdf",
            1024L,
            tempDir.resolve("uploads/socios/1/123456_abc_documento.pdf").toString()
        );
        arquivoTeste.setId(1L);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Limpa arquivos de teste criados
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignora erros de limpeza
                    }
                });
        }
    }
    
    // ============================================
    // Testes de Upload de Arquivos
    // ============================================
    
    @Test
    void uploadArquivos_ComArquivoValido_DeveSalvarComSucesso() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "documento.pdf",
            "application/pdf",
            "Conteúdo do PDF".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(1L);
            return arquivo;
        });
        
        // Act
        List<Arquivo> resultado = arquivoService.uploadArquivos(1L, new MultipartFile[]{file});
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNomeOriginal()).isEqualTo("documento.pdf");
        assertThat(resultado.get(0).getTipoConteudo()).isEqualTo("application/pdf");
        assertThat(resultado.get(0).getTamanho()).isEqualTo(file.getSize());
        
        verify(socioRepository).findById(1L);
        verify(arquivoRepository).sumTamanhoBySocioId(1L);
        verify(arquivoRepository).save(any(Arquivo.class));
    }
    
    @Test
    void uploadArquivos_ComMultiplosArquivos_DeveSalvarTodos() throws IOException {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "file1", "doc1.pdf", "application/pdf", "Conteúdo 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file2", "imagem.jpg", "image/jpeg", "Conteúdo 2".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(System.currentTimeMillis());
            return arquivo;
        });
        
        // Act
        List<Arquivo> resultado = arquivoService.uploadArquivos(1L, new MultipartFile[]{file1, file2});
        
        // Assert
        assertThat(resultado).hasSize(2);
        verify(arquivoRepository, times(2)).save(any(Arquivo.class));
    }
    
    @Test
    void uploadArquivos_ComSocioInexistente_DeveLancarExcecao() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "documento.pdf", "application/pdf", "Conteúdo".getBytes()
        );
        
        when(socioRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(999L, new MultipartFile[]{file}))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Socio")
            .hasMessageContaining("999");
        
        verify(socioRepository).findById(999L);
        verify(arquivoRepository, never()).save(any());
    }
    
    @Test
    void uploadArquivos_SemArquivos_DeveLancarExcecao() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(1L, new MultipartFile[]{}))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Nenhum arquivo foi enviado");
        
        verify(arquivoRepository, never()).save(any());
    }
    
    @Test
    void uploadArquivos_ExcedendoLimiteTotal_DeveLancarExcecao() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "documento.pdf", "application/pdf", new byte[1024]
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        // Simula que o sócio já tem quase o limite máximo
        when(arquivoRepository.sumTamanhoBySocioId(1L))
            .thenReturn(ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(1L, new MultipartFile[]{file}))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Limite de armazenamento excedido");
        
        verify(arquivoRepository, never()).save(any());
    }
    
    // ============================================
    // Testes de Validação de Arquivos
    // ============================================
    
    @Test
    void uploadArquivos_ComArquivoVazio_DeveLancarExcecao() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "vazio.pdf", "application/pdf", new byte[0]
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(1L, new MultipartFile[]{file}))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("vazio");
        
        verify(arquivoRepository, never()).save(any());
    }
    
    @Test
    void uploadArquivos_ComTipoInvalido_DeveLancarExcecao() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "script.exe", "application/x-msdownload", "Conteúdo".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(1L, new MultipartFile[]{file}))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("não permitido");
        
        verify(arquivoRepository, never()).save(any());
    }
    
    @Test
    void uploadArquivos_ComTamanhoExcessivo_DeveLancarExcecao() {
        // Arrange
        byte[] conteudoGrande = new byte[(int) (ArquivoConstants.TAMANHO_MAXIMO_ARQUIVO + 1)];
        MockMultipartFile file = new MockMultipartFile(
            "file", "grande.pdf", "application/pdf", conteudoGrande
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(1L, new MultipartFile[]{file}))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("tamanho máximo");
        
        verify(arquivoRepository, never()).save(any());
    }
    
    @Test
    void uploadArquivos_ComNomeInvalido_DeveLancarExcecao() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "../../../etc/passwd", "application/pdf", "Conteúdo".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.uploadArquivos(1L, new MultipartFile[]{file}))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Nome do arquivo");
        
        verify(arquivoRepository, never()).save(any());
    }
    
    // ============================================
    // Testes de Validação de Tipo e Tamanho
    // ============================================
    
    @Test
    void validarTipoArquivo_ComTipoPermitido_DeveRetornarTrue() {
        // Arrange & Act & Assert
        assertThat(arquivoService.validarTipoArquivo("application/pdf")).isTrue();
        assertThat(arquivoService.validarTipoArquivo("image/jpeg")).isTrue();
        assertThat(arquivoService.validarTipoArquivo("image/png")).isTrue();
        assertThat(arquivoService.validarTipoArquivo("application/msword")).isTrue();
    }
    
    @Test
    void validarTipoArquivo_ComTipoNaoPermitido_DeveRetornarFalse() {
        // Arrange & Act & Assert
        assertThat(arquivoService.validarTipoArquivo("application/x-msdownload")).isFalse();
        assertThat(arquivoService.validarTipoArquivo("application/x-executable")).isFalse();
        assertThat(arquivoService.validarTipoArquivo("video/mp4")).isFalse();
    }
    
    @Test
    void validarTipoArquivo_ComTipoNulo_DeveRetornarFalse() {
        // Arrange & Act & Assert
        assertThat(arquivoService.validarTipoArquivo(null)).isFalse();
    }
    
    @Test
    void validarTamanhoArquivo_ComTamanhoValido_DeveRetornarTrue() {
        // Arrange & Act & Assert
        assertThat(arquivoService.validarTamanhoArquivo(1024L)).isTrue();
        assertThat(arquivoService.validarTamanhoArquivo(5 * 1024 * 1024L)).isTrue();
        assertThat(arquivoService.validarTamanhoArquivo(ArquivoConstants.TAMANHO_MAXIMO_ARQUIVO)).isTrue();
    }
    
    @Test
    void validarTamanhoArquivo_ComTamanhoInvalido_DeveRetornarFalse() {
        // Arrange & Act & Assert
        assertThat(arquivoService.validarTamanhoArquivo(0L)).isFalse();
        assertThat(arquivoService.validarTamanhoArquivo(-1L)).isFalse();
        assertThat(arquivoService.validarTamanhoArquivo(ArquivoConstants.TAMANHO_MAXIMO_ARQUIVO + 1)).isFalse();
    }
    
    // ============================================
    // Testes de Listagem de Arquivos
    // ============================================
    
    @Test
    void listarArquivos_ComSocioExistente_DeveRetornarLista() {
        // Arrange
        Arquivo arquivo1 = new Arquivo(socioTeste, "doc1.pdf", "123_doc1.pdf", 
                                       "application/pdf", 1024L, "/path/doc1.pdf");
        Arquivo arquivo2 = new Arquivo(socioTeste, "doc2.pdf", "456_doc2.pdf", 
                                       "application/pdf", 2048L, "/path/doc2.pdf");
        
        when(socioRepository.existsById(1L)).thenReturn(true);
        when(arquivoRepository.findBySocioId(1L)).thenReturn(Arrays.asList(arquivo1, arquivo2));
        
        // Act
        List<Arquivo> resultado = arquivoService.listarArquivos(1L);
        
        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(arquivo1, arquivo2);
        
        verify(socioRepository).existsById(1L);
        verify(arquivoRepository).findBySocioId(1L);
    }
    
    @Test
    void listarArquivos_ComSocioInexistente_DeveLancarExcecao() {
        // Arrange
        when(socioRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.listarArquivos(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Socio")
            .hasMessageContaining("999");
        
        verify(socioRepository).existsById(999L);
        verify(arquivoRepository, never()).findBySocioId(any());
    }
    
    @Test
    void listarArquivos_SemArquivos_DeveRetornarListaVazia() {
        // Arrange
        when(socioRepository.existsById(1L)).thenReturn(true);
        when(arquivoRepository.findBySocioId(1L)).thenReturn(List.of());
        
        // Act
        List<Arquivo> resultado = arquivoService.listarArquivos(1L);
        
        // Assert
        assertThat(resultado).isEmpty();
        
        verify(arquivoRepository).findBySocioId(1L);
    }
    
    // ============================================
    // Testes de Busca de Arquivo
    // ============================================
    
    @Test
    void buscarArquivo_ComIdExistente_DeveRetornarArquivo() {
        // Arrange
        when(arquivoRepository.findById(1L)).thenReturn(Optional.of(arquivoTeste));
        
        // Act
        Arquivo resultado = arquivoService.buscarArquivo(1L);
        
        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNomeOriginal()).isEqualTo("documento.pdf");
        
        verify(arquivoRepository).findById(1L);
    }
    
    @Test
    void buscarArquivo_ComIdInexistente_DeveLancarExcecao() {
        // Arrange
        when(arquivoRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.buscarArquivo(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Arquivo")
            .hasMessageContaining("999");
        
        verify(arquivoRepository).findById(999L);
    }
    
    // ============================================
    // Testes de Download de Arquivo
    // ============================================
    
    @Test
    void downloadArquivo_ComArquivoExistente_DeveRetornarResource() throws IOException {
        // Arrange
        Path arquivoFisico = tempDir.resolve("teste.pdf");
        Files.createDirectories(arquivoFisico.getParent());
        Files.write(arquivoFisico, "Conteúdo do arquivo".getBytes());
        
        Arquivo arquivo = new Arquivo(
            socioTeste, "teste.pdf", "123_teste.pdf", 
            "application/pdf", 100L, arquivoFisico.toString()
        );
        arquivo.setId(1L);
        
        when(arquivoRepository.findById(1L)).thenReturn(Optional.of(arquivo));
        
        // Act
        Resource resource = arquivoService.downloadArquivo(1L);
        
        // Assert
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();
        
        verify(arquivoRepository).findById(1L);
    }
    
    @Test
    void downloadArquivo_ComArquivoFisicoInexistente_DeveLancarExcecao() {
        // Arrange
        Arquivo arquivo = new Arquivo(
            socioTeste, "inexistente.pdf", "123_inexistente.pdf",
            "application/pdf", 100L, "/caminho/inexistente/arquivo.pdf"
        );
        arquivo.setId(1L);
        
        when(arquivoRepository.findById(1L)).thenReturn(Optional.of(arquivo));
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.downloadArquivo(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("não encontrado no sistema de arquivos");
        
        verify(arquivoRepository).findById(1L);
    }
    
    // ============================================
    // Testes de Exclusão de Arquivo
    // ============================================
    
    @Test
    void excluirArquivo_ComArquivoExistente_DeveExcluirComSucesso() throws IOException {
        // Arrange
        Path arquivoFisico = tempDir.resolve("teste.pdf");
        Files.createDirectories(arquivoFisico.getParent());
        Files.write(arquivoFisico, "Conteúdo".getBytes());
        
        Arquivo arquivo = new Arquivo(
            socioTeste, "teste.pdf", "123_teste.pdf",
            "application/pdf", 100L, arquivoFisico.toString()
        );
        arquivo.setId(1L);
        
        when(arquivoRepository.findById(1L)).thenReturn(Optional.of(arquivo));
        doNothing().when(arquivoRepository).delete(arquivo);
        
        // Act
        arquivoService.excluirArquivo(1L);
        
        // Assert
        assertThat(Files.exists(arquivoFisico)).isFalse();
        
        verify(arquivoRepository).findById(1L);
        verify(arquivoRepository).delete(arquivo);
    }
    
    @Test
    void excluirArquivo_ComIdInexistente_DeveLancarExcecao() {
        // Arrange
        when(arquivoRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.excluirArquivo(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Arquivo")
            .hasMessageContaining("999");
        
        verify(arquivoRepository).findById(999L);
        verify(arquivoRepository, never()).delete(any());
    }
    
    @Test
    void excluirArquivo_ComArquivoFisicoInexistente_DeveExcluirRegistroBanco() {
        // Arrange
        Arquivo arquivo = new Arquivo(
            socioTeste, "inexistente.pdf", "123_inexistente.pdf",
            "application/pdf", 100L, "/caminho/inexistente.pdf"
        );
        arquivo.setId(1L);
        
        when(arquivoRepository.findById(1L)).thenReturn(Optional.of(arquivo));
        doNothing().when(arquivoRepository).delete(arquivo);
        
        // Act
        arquivoService.excluirArquivo(1L);
        
        // Assert - deve excluir do banco mesmo se arquivo físico não existir
        verify(arquivoRepository).findById(1L);
        verify(arquivoRepository).delete(arquivo);
    }
    
    // ============================================
    // Testes de Estatísticas
    // ============================================
    
    @Test
    void obterEstatisticas_ComSocioExistente_DeveRetornarEstatisticas() {
        // Arrange
        when(socioRepository.existsById(1L)).thenReturn(true);
        when(arquivoRepository.countBySocioId(1L)).thenReturn(5L);
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(50 * 1024 * 1024L); // 50 MB
        
        // Act
        ArquivoService.ArquivoStatistics stats = arquivoService.obterEstatisticas(1L);
        
        // Assert
        assertThat(stats.getQuantidadeArquivos()).isEqualTo(5L);
        assertThat(stats.getTamanhoTotal()).isEqualTo(50 * 1024 * 1024L);
        assertThat(stats.getEspacoDisponivel()).isEqualTo(50 * 1024 * 1024L); // 50 MB disponível
        assertThat(stats.getLimiteTotal()).isEqualTo(ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO);
        assertThat(stats.getPercentualUtilizado()).isEqualTo(50.0);
        
        verify(socioRepository).existsById(1L);
        verify(arquivoRepository).countBySocioId(1L);
        verify(arquivoRepository).sumTamanhoBySocioId(1L);
    }
    
    @Test
    void obterEstatisticas_ComSocioSemArquivos_DeveRetornarEstatisticasZeradas() {
        // Arrange
        when(socioRepository.existsById(1L)).thenReturn(true);
        when(arquivoRepository.countBySocioId(1L)).thenReturn(0L);
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        
        // Act
        ArquivoService.ArquivoStatistics stats = arquivoService.obterEstatisticas(1L);
        
        // Assert
        assertThat(stats.getQuantidadeArquivos()).isZero();
        assertThat(stats.getTamanhoTotal()).isZero();
        assertThat(stats.getEspacoDisponivel()).isEqualTo(ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO);
        assertThat(stats.getPercentualUtilizado()).isZero();
    }
    
    @Test
    void obterEstatisticas_ComSocioInexistente_DeveLancarExcecao() {
        // Arrange
        when(socioRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> arquivoService.obterEstatisticas(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Socio")
            .hasMessageContaining("999");
        
        verify(socioRepository).existsById(999L);
    }
    
    // ============================================
    // Testes de Tipos de Arquivo Específicos
    // ============================================
    
    @Test
    void uploadArquivos_ComImagemJPEG_DeveSalvarComSucesso() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "foto.jpg", "image/jpeg", "Conteúdo da imagem".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(1L);
            return arquivo;
        });
        
        // Act
        List<Arquivo> resultado = arquivoService.uploadArquivos(1L, new MultipartFile[]{file});
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoConteudo()).isEqualTo("image/jpeg");
    }
    
    @Test
    void uploadArquivos_ComDocumentoWord_DeveSalvarComSucesso() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "documento.docx", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "Conteúdo do Word".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(1L);
            return arquivo;
        });
        
        // Act
        List<Arquivo> resultado = arquivoService.uploadArquivos(1L, new MultipartFile[]{file});
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoConteudo())
            .isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
    
    @Test
    void uploadArquivos_ComArquivoZIP_DeveSalvarComSucesso() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "documentos.zip", "application/zip", "Conteúdo ZIP".getBytes()
        );
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socioTeste));
        when(arquivoRepository.sumTamanhoBySocioId(1L)).thenReturn(0L);
        when(arquivoRepository.save(any(Arquivo.class))).thenAnswer(invocation -> {
            Arquivo arquivo = invocation.getArgument(0);
            arquivo.setId(1L);
            return arquivo;
        });
        
        // Act
        List<Arquivo> resultado = arquivoService.uploadArquivos(1L, new MultipartFile[]{file});
        
        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoConteudo()).isEqualTo("application/zip");
    }
}
