package com.sindicato.repository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.sindicato.model.Arquivo;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;

/**
 * Testes unitários para ArquivoRepository.
 * Valida operações de persistência e consultas customizadas.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ArquivoRepository Tests")
class ArquivoRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ArquivoRepository arquivoRepository;
    
    private Socio socio;
    
    @BeforeEach
    void setUp() {
        // Criar sócio para testes
        socio = new Socio();
        socio.setNome("João da Silva");
        socio.setCpf("123.456.789-00");
        socio.setMatricula("MAT001");
        socio.setStatus(StatusSocio.ATIVO);
        socio.setDataNascimento(LocalDate.of(1980, 1, 1));
        entityManager.persist(socio);
        entityManager.flush();
    }
    
    @Test
    @DisplayName("Should save arquivo successfully")
    void shouldSaveArquivo() {
        // Given
        Arquivo arquivo = new Arquivo();
        arquivo.setSocio(socio);
        arquivo.setNomeOriginal("documento.pdf");
        arquivo.setNomeArquivo("123456_documento.pdf");
        arquivo.setTipoConteudo("application/pdf");
        arquivo.setTamanho(1024L);
        arquivo.setCaminhoArquivo("/uploads/socios/1/123456_documento.pdf");
        
        // When
        Arquivo saved = arquivoRepository.save(arquivo);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCriadoEm()).isNotNull();
        assertThat(saved.getNomeOriginal()).isEqualTo("documento.pdf");
    }
    
    @Test
    @DisplayName("Should find arquivos by socio id")
    void shouldFindArquivosBySocioId() {
        // Given
        Arquivo arquivo1 = createArquivo("doc1.pdf", "application/pdf", 1024L);
        Arquivo arquivo2 = createArquivo("doc2.pdf", "application/pdf", 2048L);
        entityManager.persist(arquivo1);
        entityManager.persist(arquivo2);
        entityManager.flush();
        
        // When
        List<Arquivo> arquivos = arquivoRepository.findBySocioId(socio.getId());
        
        // Then
        assertThat(arquivos).hasSize(2);
        assertThat(arquivos).extracting(Arquivo::getNomeOriginal)
            .containsExactlyInAnyOrder("doc1.pdf", "doc2.pdf");
    }
    
    @Test
    @DisplayName("Should find arquivos by tipo conteudo")
    void shouldFindArquivosByTipoConteudo() {
        // Given
        Arquivo pdf = createArquivo("doc.pdf", "application/pdf", 1024L);
        Arquivo jpg = createArquivo("foto.jpg", "image/jpeg", 2048L);
        entityManager.persist(pdf);
        entityManager.persist(jpg);
        entityManager.flush();
        
        // When
        List<Arquivo> pdfs = arquivoRepository.findByTipoConteudo("application/pdf");
        
        // Then
        assertThat(pdfs).hasSize(1);
        assertThat(pdfs.get(0).getNomeOriginal()).isEqualTo("doc.pdf");
    }
    
    @Test
    @DisplayName("Should count arquivos by socio id")
    void shouldCountArquivosBySocioId() {
        // Given
        Arquivo arquivo1 = createArquivo("doc1.pdf", "application/pdf", 1024L);
        Arquivo arquivo2 = createArquivo("doc2.pdf", "application/pdf", 2048L);
        entityManager.persist(arquivo1);
        entityManager.persist(arquivo2);
        entityManager.flush();
        
        // When
        long count = arquivoRepository.countBySocioId(socio.getId());
        
        // Then
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Should sum tamanho by socio id")
    void shouldSumTamanhoBySocioId() {
        // Given
        Arquivo arquivo1 = createArquivo("doc1.pdf", "application/pdf", 1024L);
        Arquivo arquivo2 = createArquivo("doc2.pdf", "application/pdf", 2048L);
        entityManager.persist(arquivo1);
        entityManager.persist(arquivo2);
        entityManager.flush();
        
        // When
        Long totalSize = arquivoRepository.sumTamanhoBySocioId(socio.getId());
        
        // Then
        assertThat(totalSize).isEqualTo(3072L);
    }
    
    @Test
    @DisplayName("Should return zero when no arquivos exist for socio")
    void shouldReturnZeroWhenNoArquivosExist() {
        // When
        Long totalSize = arquivoRepository.sumTamanhoBySocioId(socio.getId());
        
        // Then
        assertThat(totalSize).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("Should check if arquivo exists by socio id and nome arquivo")
    void shouldCheckIfArquivoExists() {
        // Given
        Arquivo arquivo = createArquivo("doc.pdf", "application/pdf", 1024L);
        arquivo.setNomeArquivo("unique_doc.pdf");
        entityManager.persist(arquivo);
        entityManager.flush();
        
        // When
        boolean exists = arquivoRepository.existsBySocioIdAndNomeArquivo(socio.getId(), "unique_doc.pdf");
        boolean notExists = arquivoRepository.existsBySocioIdAndNomeArquivo(socio.getId(), "other.pdf");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
    
    @Test
    @DisplayName("Should find arquivos by socio id and tipo conteudo")
    void shouldFindArquivosBySocioIdAndTipoConteudo() {
        // Given
        Arquivo pdf = createArquivo("doc.pdf", "application/pdf", 1024L);
        Arquivo jpg = createArquivo("foto.jpg", "image/jpeg", 2048L);
        entityManager.persist(pdf);
        entityManager.persist(jpg);
        entityManager.flush();
        
        // When
        List<Arquivo> pdfs = arquivoRepository.findBySocioIdAndTipoConteudo(socio.getId(), "application/pdf");
        
        // Then
        assertThat(pdfs).hasSize(1);
        assertThat(pdfs.get(0).getNomeOriginal()).isEqualTo("doc.pdf");
    }
    
    // Helper method
    private Arquivo createArquivo(String nomeOriginal, String tipoConteudo, Long tamanho) {
        Arquivo arquivo = new Arquivo();
        arquivo.setSocio(socio);
        arquivo.setNomeOriginal(nomeOriginal);
        arquivo.setNomeArquivo("generated_" + nomeOriginal);
        arquivo.setTipoConteudo(tipoConteudo);
        arquivo.setTamanho(tamanho);
        arquivo.setCaminhoArquivo("/uploads/socios/" + socio.getId() + "/" + nomeOriginal);
        return arquivo;
    }
}
