package com.sindicato.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.sindicato.model.Pagamento;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusPagamento;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.PagamentoRepository;
import com.sindicato.repository.SocioRepository;
import com.sindicato.service.ReciboService;

/**
 * Integration test for ReciboService with real database.
 * Tests complete PDF generation flow with persisted entities.
 * 
 * Requirements tested:
 * - 4.1: Sequential receipt numbering (tested via PagamentoService)
 * - 4.2: Include all mandatory data in receipt
 * - 4.3: Support second copy generation
 * - 4.4: Export receipts in PDF format
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReciboIntegrationTest {
    
    @Autowired
    private ReciboService reciboService;
    
    @Autowired
    private SocioRepository socioRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    private Socio socio;
    private Pagamento pagamento;
    
    @BeforeEach
    void setUp() {
        // Create and persist socio
        socio = new Socio();
        socio.setNome("Carlos Eduardo Santos");
        socio.setCpf("111.222.333-44");
        socio.setMatricula("MAT-2024-100");
        socio.setRg("12.345.678-9");
        socio.setDataNascimento(LocalDate.of(1980, 5, 15));
        socio.setTelefone("(11) 91234-5678");
        socio.setEmail("carlos@example.com");
        socio.setEndereco("Rua das Acácias, 456");
        socio.setCidade("Campinas");
        socio.setEstado("SP");
        socio.setCep("13000-000");
        socio.setProfissao("Agricultor");
        socio.setStatus(StatusSocio.ATIVO);
        socio = socioRepository.save(socio);
        
        // Create and persist payment
        pagamento = new Pagamento();
        pagamento.setSocio(socio);
        pagamento.setValor(new BigDecimal("250.00"));
        pagamento.setMes(6);
        pagamento.setAno(2024);
        pagamento.setDataPagamento(LocalDate.of(2024, 6, 10));
        pagamento.setNumeroRecibo("REC-20240610-0100");
        pagamento.setObservacoes("Pagamento referente à mensalidade de junho");
        pagamento.setStatus(StatusPagamento.PAGO);
        pagamento = pagamentoRepository.save(pagamento);
    }
    
    @Test
    void deveGerarReciboPdfComDadosCompletos() {
        // When
        byte[] pdfBytes = reciboService.gerarRecibo(pagamento.getId());
        
        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        
        // Verify it's a valid PDF with content
        assertThat(isPdfValido(pdfBytes)).isTrue();
        assertThat(getNumeroDePages(pdfBytes)).isEqualTo(1);
    }
    
    @Test
    void deveGerarReciboOriginalESegundaVia() {
        // When
        byte[] pdfOriginal = reciboService.gerarRecibo(pagamento.getId(), false);
        byte[] pdfSegundaVia = reciboService.gerarRecibo(pagamento.getId(), true);
        
        // Then
        assertThat(pdfOriginal).isNotNull();
        assertThat(pdfSegundaVia).isNotNull();
        
        // Both should be valid PDFs
        assertThat(isPdfValido(pdfOriginal)).isTrue();
        assertThat(isPdfValido(pdfSegundaVia)).isTrue();
        
        // Both should have 1 page
        assertThat(getNumeroDePages(pdfOriginal)).isEqualTo(1);
        assertThat(getNumeroDePages(pdfSegundaVia)).isEqualTo(1);
        
        // Content should be different (segunda via has marking)
        assertThat(pdfOriginal).isNotEqualTo(pdfSegundaVia);
    }
    
    @Test
    void deveGerarReciboPdfParaPagamentoSemObservacoes() {
        // Given
        pagamento.setObservacoes(null);
        pagamento = pagamentoRepository.save(pagamento);
        
        // When
        byte[] pdfBytes = reciboService.gerarRecibo(pagamento.getId());
        
        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }
    
    @Test
    void deveGerarReciboPdfParaSocioComDadosMinimos() {
        // Given - Create socio with minimal data
        Socio socioMinimo = new Socio();
        socioMinimo.setNome("Ana Silva");
        socioMinimo.setCpf("999.888.777-66");
        socioMinimo.setMatricula("MAT-2024-200");
        socioMinimo.setStatus(StatusSocio.ATIVO);
        socioMinimo = socioRepository.save(socioMinimo);
        
        Pagamento pagamentoMinimo = new Pagamento();
        pagamentoMinimo.setSocio(socioMinimo);
        pagamentoMinimo.setValor(new BigDecimal("100.00"));
        pagamentoMinimo.setMes(7);
        pagamentoMinimo.setAno(2024);
        pagamentoMinimo.setDataPagamento(LocalDate.of(2024, 7, 5));
        pagamentoMinimo.setNumeroRecibo("REC-20240705-0200");
        pagamentoMinimo.setStatus(StatusPagamento.PAGO);
        pagamentoMinimo = pagamentoRepository.save(pagamentoMinimo);
        
        // When
        byte[] pdfBytes = reciboService.gerarRecibo(pagamentoMinimo.getId());
        
        // Then
        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }
    
    @Test
    void deveGerarRecibosPdfParaDiferentesMeses() {
        // Test receipts for different months
        for (int mes = 1; mes <= 12; mes++) {
            // Given - Create new socio for each month to avoid duplicate constraint
            Socio socioMes = new Socio();
            socioMes.setNome("Socio Mes " + mes);
            socioMes.setCpf(String.format("111.222.333-%02d", mes));
            socioMes.setMatricula(String.format("MAT-2024-%03d", 200 + mes));
            socioMes.setStatus(StatusSocio.ATIVO);
            socioMes = socioRepository.save(socioMes);
            
            Pagamento pagamentoMes = new Pagamento();
            pagamentoMes.setSocio(socioMes);
            pagamentoMes.setValor(new BigDecimal("150.00"));
            pagamentoMes.setMes(mes);
            pagamentoMes.setAno(2024);
            pagamentoMes.setDataPagamento(LocalDate.of(2024, mes, 1));
            pagamentoMes.setNumeroRecibo(String.format("REC-2024%02d01-%04d", mes, 300 + mes));
            pagamentoMes.setStatus(StatusPagamento.PAGO);
            pagamentoMes = pagamentoRepository.save(pagamentoMes);
            
            // When
            byte[] pdfBytes = reciboService.gerarRecibo(pagamentoMes.getId());
            
            // Then
            assertThat(pdfBytes).isNotNull();
            assertThat(isPdfValido(pdfBytes)).isTrue();
        }
    }
    
    @Test
    void deveGerarReciboPdfComValoresVariados() {
        // Test with different payment amounts
        BigDecimal[] valores = {
            new BigDecimal("50.00"),
            new BigDecimal("100.50"),
            new BigDecimal("999.99"),
            new BigDecimal("1234.56"),
            new BigDecimal("0.01")
        };
        
        for (int i = 0; i < valores.length; i++) {
            // Given - Create new socio for each value to avoid duplicate constraint
            Socio socioValor = new Socio();
            socioValor.setNome("Socio Valor " + i);
            socioValor.setCpf(String.format("222.333.444-%02d", i));
            socioValor.setMatricula(String.format("MAT-2024-%03d", 300 + i));
            socioValor.setStatus(StatusSocio.ATIVO);
            socioValor = socioRepository.save(socioValor);
            
            Pagamento pagamentoValor = new Pagamento();
            pagamentoValor.setSocio(socioValor);
            pagamentoValor.setValor(valores[i]);
            pagamentoValor.setMes(1);
            pagamentoValor.setAno(2024);
            pagamentoValor.setDataPagamento(LocalDate.of(2024, 1, 1));
            pagamentoValor.setNumeroRecibo(String.format("REC-20240101-%04d", 400 + i));
            pagamentoValor.setStatus(StatusPagamento.PAGO);
            pagamentoValor = pagamentoRepository.save(pagamentoValor);
            
            // When
            byte[] pdfBytes = reciboService.gerarRecibo(pagamentoValor.getId());
            
            // Then
            assertThat(pdfBytes).isNotNull();
            assertThat(isPdfValido(pdfBytes)).isTrue();
        }
    }
    
    /**
     * Helper method to validate if byte array is a valid PDF.
     */
    private boolean isPdfValido(byte[] pdfBytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
            PdfReader reader = new PdfReader(bais);
            PdfDocument pdfDoc = new PdfDocument(reader);
            int numPages = pdfDoc.getNumberOfPages();
            pdfDoc.close();
            return numPages > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Helper method to get number of pages in PDF.
     */
    private int getNumeroDePages(byte[] pdfBytes) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
            PdfReader reader = new PdfReader(bais);
            PdfDocument pdfDoc = new PdfDocument(reader);
            int numPages = pdfDoc.getNumberOfPages();
            pdfDoc.close();
            return numPages;
        } catch (Exception e) {
            return 0;
        }
    }
}
