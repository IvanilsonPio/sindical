package com.sindicato.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sindicato.dto.PagamentoRequest;
import com.sindicato.dto.PagamentoResponse;
import com.sindicato.model.StatusPagamento;
import com.sindicato.service.PagamentoService;

/**
 * Unit tests for PagamentoController.
 * Tests REST endpoints for payment management operations.
 */
@WebMvcTest(PagamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PagamentoControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private PagamentoService pagamentoService;
    
    private PagamentoResponse pagamentoResponse;
    private PagamentoRequest pagamentoRequest;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        pagamentoResponse = new PagamentoResponse();
        pagamentoResponse.setId(1L);
        pagamentoResponse.setSocioId(1L);
        pagamentoResponse.setSocioNome("João Silva");
        pagamentoResponse.setSocioCpf("123.456.789-00");
        pagamentoResponse.setValor(new BigDecimal("50.00"));
        pagamentoResponse.setMes(3);
        pagamentoResponse.setAno(2024);
        pagamentoResponse.setDataPagamento(LocalDate.of(2024, 3, 15));
        pagamentoResponse.setNumeroRecibo("REC-20240315-0001");
        pagamentoResponse.setStatus(StatusPagamento.PAGO);
        
        pagamentoRequest = new PagamentoRequest();
        pagamentoRequest.setSocioId(1L);
        pagamentoRequest.setValor(new BigDecimal("50.00"));
        pagamentoRequest.setMes(3);
        pagamentoRequest.setAno(2024);
        pagamentoRequest.setDataPagamento(LocalDate.of(2024, 3, 15));
    }
    
    @Test
    void listarPagamentosShouldReturnPageOfPayments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<PagamentoResponse> page = new PageImpl<>(java.util.List.of(pagamentoResponse), pageable, 1);
        when(pagamentoService.listarPagamentos(any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].socioNome").value("João Silva"))
                .andExpect(jsonPath("$.content[0].numeroRecibo").value("REC-20240315-0001"));
        
        verify(pagamentoService).listarPagamentos(any(Pageable.class));
    }
    
    @Test
    void listarPagamentosWithSocioFilterShouldReturnFilteredPayments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<PagamentoResponse> page = new PageImpl<>(java.util.List.of(pagamentoResponse), pageable, 1);
        when(pagamentoService.listarPagamentosPorSocio(eq(1L), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos")
                .param("socioId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].socioId").value(1));
        
        verify(pagamentoService).listarPagamentosPorSocio(eq(1L), any(Pageable.class));
    }
    
    @Test
    void listarPagamentosWithPeriodFilterShouldReturnFilteredPayments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<PagamentoResponse> page = new PageImpl<>(java.util.List.of(pagamentoResponse), pageable, 1);
        when(pagamentoService.listarPagamentosPorPeriodo(eq(3), eq(2024), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos")
                .param("mes", "3")
                .param("ano", "2024")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].mes").value(3))
                .andExpect(jsonPath("$.content[0].ano").value(2024));
        
        verify(pagamentoService).listarPagamentosPorPeriodo(eq(3), eq(2024), any(Pageable.class));
    }
    
    @Test
    void listarPagamentosWithStatusFilterShouldReturnFilteredPayments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<PagamentoResponse> page = new PageImpl<>(java.util.List.of(pagamentoResponse), pageable, 1);
        when(pagamentoService.listarPagamentosPorStatus(eq(StatusPagamento.PAGO), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos")
                .param("status", "PAGO")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PAGO"));
        
        verify(pagamentoService).listarPagamentosPorStatus(eq(StatusPagamento.PAGO), any(Pageable.class));
    }
    
    @Test
    void buscarPagamentoShouldReturnPayment() throws Exception {
        // Arrange
        when(pagamentoService.buscarPorId(1L)).thenReturn(pagamentoResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.socioNome").value("João Silva"));
        
        verify(pagamentoService).buscarPorId(1L);
    }
    
    @Test
    void buscarPorReciboShouldReturnPayment() throws Exception {
        // Arrange
        when(pagamentoService.buscarPorNumeroRecibo("REC-20240315-0001")).thenReturn(pagamentoResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/recibo/REC-20240315-0001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroRecibo").value("REC-20240315-0001"));
        
        verify(pagamentoService).buscarPorNumeroRecibo("REC-20240315-0001");
    }
    
    @Test
    void listarInadimplentesShouldReturnDelinquentMembers() throws Exception {
        // Arrange
        Map<String, Object> inadimplente = new HashMap<>();
        inadimplente.put("socioId", 1L);
        inadimplente.put("nome", "João Silva");
        inadimplente.put("cpf", "123.456.789-00");
        
        Pageable pageable = PageRequest.of(0, 20);
        Page<Map<String, Object>> page = new PageImpl<>(java.util.List.of(inadimplente), pageable, 1);
        when(pagamentoService.listarInadimplentes(any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/inadimplentes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].socioId").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("João Silva"));
        
        verify(pagamentoService).listarInadimplentes(any(Pageable.class));
    }
    
    @Test
    void registrarPagamentoShouldCreatePayment() throws Exception {
        // Arrange
        when(pagamentoService.registrarPagamento(any(PagamentoRequest.class))).thenReturn(pagamentoResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numeroRecibo").value("REC-20240315-0001"));
        
        verify(pagamentoService).registrarPagamento(any(PagamentoRequest.class));
    }
    
    @Test
    void registrarPagamentoWithInvalidDataShouldReturnBadRequest() throws Exception {
        // Arrange - invalid request with missing required fields
        PagamentoRequest invalidRequest = new PagamentoRequest();
        
        // Act & Assert
        mockMvc.perform(post("/api/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void cancelarPagamentoShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(pagamentoService).cancelarPagamento(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/pagamentos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(pagamentoService).cancelarPagamento(1L);
    }
    
    @Test
    void gerarReciboShouldReturnPdfFile() throws Exception {
        // Arrange
        byte[] pdfContent = "PDF content".getBytes();
        when(pagamentoService.gerarReciboPdf(1L)).thenReturn(pdfContent);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/1/recibo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(pagamentoService).gerarReciboPdf(1L);
    }
    
    @Test
    void gerarReciboSegundaViaShouldReturnPdfFile() throws Exception {
        // Arrange
        byte[] pdfContent = "PDF content with SEGUNDA VIA".getBytes();
        when(pagamentoService.gerarReciboSegundaVia(1L)).thenReturn(pdfContent);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/1/recibo/segunda-via")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(pagamentoService).gerarReciboSegundaVia(1L);
    }
    
    @Test
    void listarPagamentosPorSocioShouldReturnPageOfPayments() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 100);
        Page<PagamentoResponse> page = new PageImpl<>(java.util.List.of(pagamentoResponse), pageable, 1);
        when(pagamentoService.listarPagamentosPorSocio(eq(1L), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/socio/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].socioNome").value("João Silva"));
        
        verify(pagamentoService).listarPagamentosPorSocio(eq(1L), any(Pageable.class));
    }
    
    @Test
    void checkPagamentoExistsShouldReturnBoolean() throws Exception {
        // Arrange
        when(pagamentoService.pagamentoJaExiste(1L, 3, 2024)).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/check-pagamento")
                .param("socioId", "1")
                .param("mes", "3")
                .param("ano", "2024")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
        
        verify(pagamentoService).pagamentoJaExiste(1L, 3, 2024);
    }
    
    @Test
    void contarPorStatusShouldReturnCount() throws Exception {
        // Arrange
        when(pagamentoService.contarPorStatus(StatusPagamento.PAGO)).thenReturn(10L);
        
        // Act & Assert
        mockMvc.perform(get("/api/pagamentos/status/PAGO/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(10));
        
        verify(pagamentoService).contarPorStatus(StatusPagamento.PAGO);
    }
}
