package com.sindicato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Pagamento;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusPagamento;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.PagamentoRepository;

@ExtendWith(MockitoExtension.class)
class ReciboServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private ReciboService reciboService;

    private Socio socio;
    private Pagamento pagamento;

    @BeforeEach
    void setUp() {
        socio = new Socio();
        socio.setId(1L);
        socio.setNome("João da Silva");
        socio.setCpf("123.456.789-00");
        socio.setMatricula("MAT-2024-001");
        socio.setTelefone("(11) 98765-4321");
        socio.setEndereco("Rua das Flores, 123");
        socio.setCidade("São Paulo");
        socio.setEstado("SP");
        socio.setStatus(StatusSocio.ATIVO);

        pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setSocio(socio);
        pagamento.setValor(new BigDecimal("150.00"));
        pagamento.setMes(3);
        pagamento.setAno(2024);
        pagamento.setDataPagamento(LocalDate.of(2024, 3, 15));
        pagamento.setNumeroRecibo("REC-20240315-0001");
        pagamento.setObservacoes("Pagamento em dia");
        pagamento.setStatus(StatusPagamento.PAGO);
    }

    @Test
    void deveGerarReciboPdfComSucesso() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(0);
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    @Test
    void deveGerarReciboSegundaViaComMarcacao() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfOriginal = reciboService.gerarRecibo(1L, false);
        byte[] pdfSegundaVia = reciboService.gerarRecibo(1L, true);

        assertThat(isPdfValido(pdfOriginal)).isTrue();
        assertThat(isPdfValido(pdfSegundaVia)).isTrue();
        assertThat(pdfOriginal).isNotEqualTo(pdfSegundaVia);
    }

    @Test
    void deveIncluirTodosDadosObrigatoriosNoRecibo() {
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        String pdfContent = new String(pdfBytes);
        assertThat(pdfContent).contains("%PDF");
        assertThat(pdfContent).contains("%%EOF");
    }

    @Test
    void deveLancarExcecaoQuandoPagamentoNaoExiste() {
        when(pagamentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reciboService.gerarRecibo(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pagamento")
                .hasMessageContaining("999");
    }

    @Test
    void deveGerarReciboPdfComDadosMinimos() {
        Socio socioMinimo = new Socio();
        socioMinimo.setId(2L);
        socioMinimo.setNome("Maria Santos");
        socioMinimo.setCpf("987.654.321-00");
        socioMinimo.setMatricula("MAT-2024-002");
        socioMinimo.setStatus(StatusSocio.ATIVO);

        Pagamento pagamentoMinimo = new Pagamento();
        pagamentoMinimo.setId(2L);
        pagamentoMinimo.setSocio(socioMinimo);
        pagamentoMinimo.setValor(new BigDecimal("100.00"));
        pagamentoMinimo.setMes(1);
        pagamentoMinimo.setAno(2024);
        pagamentoMinimo.setDataPagamento(LocalDate.of(2024, 1, 10));
        pagamentoMinimo.setNumeroRecibo("REC-20240110-0001");
        pagamentoMinimo.setStatus(StatusPagamento.PAGO);

        when(pagamentoRepository.findById(2L)).thenReturn(Optional.of(pagamentoMinimo));

        byte[] pdfBytes = reciboService.gerarRecibo(2L);

        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    @Test
    void deveGerarReciboPdfComValoresDecimais() {
        pagamento.setValor(new BigDecimal("1234.56"));
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    @Test
    void deveGerarReciboPdfParaTodosMesesDoAno() {
        for (int mes = 1; mes <= 12; mes++) {
            pagamento.setMes(mes);
            when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

            byte[] pdfBytes = reciboService.gerarRecibo(1L);

            assertThat(pdfBytes).isNotNull();
            assertThat(isPdfValido(pdfBytes)).isTrue();
        }
    }

    @Test
    void deveGerarReciboPdfComObservacoesLongas() {
        pagamento.setObservacoes("Esta é uma observação muito longa que contém várias informações " +
                "importantes sobre o pagamento realizado pelo sócio. " +
                "O pagamento foi feito em dinheiro e o recibo foi emitido imediatamente.");
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    @Test
    void deveGerarReciboPdfSemObservacoes() {
        pagamento.setObservacoes(null);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    @Test
    void deveGerarReciboPdfComEnderecoCompleto() {
        socio.setEndereco("Avenida Paulista, 1000 - Apto 501");
        socio.setCidade("São Paulo");
        socio.setEstado("SP");
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    @Test
    void deveGerarReciboPdfSemEndereco() {
        socio.setEndereco(null);
        socio.setCidade(null);
        socio.setEstado(null);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));

        byte[] pdfBytes = reciboService.gerarRecibo(1L);

        assertThat(pdfBytes).isNotNull();
        assertThat(isPdfValido(pdfBytes)).isTrue();
    }

    private boolean isPdfValido(byte[] pdfBytes) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            return doc.getNumberOfPages() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
