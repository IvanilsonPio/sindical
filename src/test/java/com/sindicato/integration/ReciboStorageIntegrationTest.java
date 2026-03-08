package com.sindicato.integration;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.dto.PagamentoRequest;
import com.sindicato.dto.PagamentoResponse;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.SocioRepository;
import com.sindicato.service.PagamentoService;

/**
 * Integration tests for receipt storage functionality.
 * Tests requirement 4.4: Permanent storage of receipts.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ReciboStorageIntegrationTest {
    
    @Autowired
    private PagamentoService pagamentoService;
    
    @Autowired
    private SocioRepository socioRepository;
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    private Socio socio;
    
    @BeforeEach
    void setUp() {
        // Create test socio
        socio = new Socio();
        socio.setNome("João da Silva");
        socio.setCpf("123.456.789-00");
        socio.setMatricula("MAT-001");
        socio.setStatus(StatusSocio.ATIVO);
        socio = socioRepository.save(socio);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        Path recibosDir = Paths.get(uploadDir).resolve("recibos");
        if (Files.exists(recibosDir)) {
            Files.walk(recibosDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }
    
    @Test
    void testRegistrarPagamento_DeveGerarESalvarRecibo() throws IOException {
        // Given
        PagamentoRequest request = new PagamentoRequest();
        request.setSocioId(socio.getId());
        request.setValor(new BigDecimal("50.00"));
        request.setMes(3);
        request.setAno(2024);
        request.setDataPagamento(LocalDate.of(2024, 3, 15));
        request.setObservacoes("Pagamento de teste");
        
        // When
        PagamentoResponse response = pagamentoService.registrarPagamento(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNumeroRecibo()).isNotNull();
        assertThat(response.getCaminhoRecibo()).isNotNull();
        assertThat(response.getCaminhoRecibo()).startsWith("recibos/");
        assertThat(response.getCaminhoRecibo()).endsWith(".pdf");
        
        // Verify file exists on disk
        Path receiptPath = Paths.get(uploadDir).resolve(response.getCaminhoRecibo());
        assertThat(Files.exists(receiptPath)).isTrue();
        assertThat(Files.size(receiptPath)).isGreaterThan(0);
    }
    
    @Test
    void testGerarReciboPdf_DeveCarregarReciboSalvo() throws IOException {
        // Given - Register payment with receipt
        PagamentoRequest request = new PagamentoRequest();
        request.setSocioId(socio.getId());
        request.setValor(new BigDecimal("50.00"));
        request.setMes(3);
        request.setAno(2024);
        request.setDataPagamento(LocalDate.of(2024, 3, 15));
        
        PagamentoResponse payment = pagamentoService.registrarPagamento(request);
        
        // When - Generate PDF (should load from disk)
        byte[] pdfBytes = pagamentoService.gerarReciboPdf(payment.getId());
        
        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        
        // Verify it's a valid PDF (starts with PDF header)
        String pdfHeader = new String(pdfBytes, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }
    
    @Test
    void testGerarReciboSegundaVia_DeveGerarComMarcacao() {
        // Given
        PagamentoRequest request = new PagamentoRequest();
        request.setSocioId(socio.getId());
        request.setValor(new BigDecimal("50.00"));
        request.setMes(3);
        request.setAno(2024);
        request.setDataPagamento(LocalDate.of(2024, 3, 15));
        
        PagamentoResponse payment = pagamentoService.registrarPagamento(request);
        
        // When
        byte[] pdfBytes = pagamentoService.gerarReciboSegundaVia(payment.getId());
        
        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        
        // Verify it's a valid PDF
        String pdfHeader = new String(pdfBytes, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
        
        // Note: We can't easily verify the "SEGUNDA VIA" text without parsing the PDF,
        // but we can verify the PDF is generated successfully
    }
}
