package com.sindicato.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sindicato.dto.PagamentoRequest;
import com.sindicato.dto.PagamentoResponse;
import com.sindicato.exception.BusinessException;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Pagamento;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusPagamento;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.PagamentoRepository;
import com.sindicato.repository.SocioRepository;

/**
 * Unit tests for PagamentoService.
 * Tests business logic for payment registration, cancellation, and status updates.
 */
@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {
    
    @Mock
    private PagamentoRepository pagamentoRepository;
    
    @Mock
    private SocioRepository socioRepository;
    
    @Mock
    private ReciboService reciboService;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private PagamentoService pagamentoService;
    
    private Socio socio;
    private PagamentoRequest pagamentoRequest;
    private Pagamento pagamento;
    
    @BeforeEach
    void setUp() {
        socio = new Socio("João Silva", "123.456.789-00", "MAT001");
        socio.setId(1L);
        socio.setStatus(StatusSocio.ATIVO);
        
        pagamentoRequest = new PagamentoRequest();
        pagamentoRequest.setSocioId(1L);
        pagamentoRequest.setValor(new BigDecimal("100.00"));
        pagamentoRequest.setMes(3);
        pagamentoRequest.setAno(2024);
        pagamentoRequest.setDataPagamento(LocalDate.of(2024, 3, 15));
        pagamentoRequest.setObservacoes("Pagamento mensal");
        
        pagamento = new Pagamento();
        pagamento.setId(1L);
        pagamento.setSocio(socio);
        pagamento.setValor(new BigDecimal("100.00"));
        pagamento.setMes(3);
        pagamento.setAno(2024);
        pagamento.setDataPagamento(LocalDate.of(2024, 3, 15));
        pagamento.setNumeroRecibo("REC-20240315-0001");
        pagamento.setStatus(StatusPagamento.PAGO);
    }
    
    @Test
    @DisplayName("Should register payment successfully")
    void shouldRegisterPaymentSuccessfully() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(false);
        when(pagamentoRepository.findTopByOrderByNumeroReciboDesc()).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);
        
        // Act
        PagamentoResponse response = pagamentoService.registrarPagamento(pagamentoRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSocioId()).isEqualTo(1L);
        assertThat(response.getValor()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getMes()).isEqualTo(3);
        assertThat(response.getAno()).isEqualTo(2024);
        assertThat(response.getStatus()).isEqualTo(StatusPagamento.PAGO);
        
        verify(pagamentoRepository).save(any(Pagamento.class));
        verify(socioRepository).save(socio);
    }
    
    @Test
    @DisplayName("Should throw exception when socio not found")
    void shouldThrowExceptionWhenSocioNotFound() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> pagamentoService.registrarPagamento(pagamentoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Socio");
        
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }
    
    @Test
    @DisplayName("Should throw exception when duplicate payment exists")
    void shouldThrowExceptionWhenDuplicatePaymentExists() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(1L, 3, 2024)).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> pagamentoService.registrarPagamento(pagamentoRequest))
                .isInstanceOf(DuplicateEntryException.class)
                .hasMessageContaining("já possui pagamento registrado");
        
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }
    
    @Test
    @DisplayName("Should throw exception when payment date is in the future")
    void shouldThrowExceptionWhenPaymentDateIsInFuture() {
        // Arrange
        pagamentoRequest.setDataPagamento(LocalDate.now().plusDays(1));
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(1L, 3, 2024)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> pagamentoService.registrarPagamento(pagamentoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Data de pagamento não pode ser no futuro");
        
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }
    
    @Test
    @DisplayName("Should cancel payment successfully")
    void shouldCancelPaymentSuccessfully() {
        // Arrange
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(false);
        
        // Act
        pagamentoService.cancelarPagamento(1L);
        
        // Assert
        ArgumentCaptor<Pagamento> pagamentoCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentoRepository, times(1)).save(pagamentoCaptor.capture());
        
        Pagamento savedPagamento = pagamentoCaptor.getValue();
        assertThat(savedPagamento.getStatus()).isEqualTo(StatusPagamento.CANCELADO);
        
        verify(socioRepository).save(socio);
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling non-existent payment")
    void shouldThrowExceptionWhenCancellingNonExistentPayment() {
        // Arrange
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> pagamentoService.cancelarPagamento(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pagamento");
        
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling already cancelled payment")
    void shouldThrowExceptionWhenCancellingAlreadyCancelledPayment() {
        // Arrange
        pagamento.setStatus(StatusPagamento.CANCELADO);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));
        
        // Act & Assert
        assertThatThrownBy(() -> pagamentoService.cancelarPagamento(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está cancelado");
        
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }
    
    @Test
    @DisplayName("Should update socio status to ATIVO when payment is registered for current month")
    void shouldUpdateSocioStatusToAtivoWhenPaymentRegisteredForCurrentMonth() {
        // Arrange
        LocalDate hoje = LocalDate.now();
        pagamentoRequest.setMes(hoje.getMonthValue());
        pagamentoRequest.setAno(hoje.getYear());
        pagamentoRequest.setDataPagamento(hoje);
        
        socio.setStatus(StatusSocio.INATIVO);
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(1L, hoje.getMonthValue(), hoje.getYear()))
                .thenReturn(false)
                .thenReturn(true);
        when(pagamentoRepository.findTopByOrderByNumeroReciboDesc()).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);
        
        // Act
        pagamentoService.registrarPagamento(pagamentoRequest);
        
        // Assert
        ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
        verify(socioRepository).save(socioCaptor.capture());
        
        Socio savedSocio = socioCaptor.getValue();
        assertThat(savedSocio.getStatus()).isEqualTo(StatusSocio.ATIVO);
    }
    
    @Test
    @DisplayName("Should update socio status to INATIVO when payment is cancelled and no recent payments")
    void shouldUpdateSocioStatusToInativoWhenPaymentCancelledAndNoRecentPayments() {
        // Arrange
        socio.setStatus(StatusSocio.ATIVO);
        
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(false);
        
        // Act
        pagamentoService.cancelarPagamento(1L);
        
        // Assert
        ArgumentCaptor<Socio> socioCaptor = ArgumentCaptor.forClass(Socio.class);
        verify(socioRepository).save(socioCaptor.capture());
        
        Socio savedSocio = socioCaptor.getValue();
        assertThat(savedSocio.getStatus()).isEqualTo(StatusSocio.INATIVO);
    }
    
    @Test
    @DisplayName("Should generate sequential receipt numbers")
    void shouldGenerateSequentialReceiptNumbers() {
        // Arrange
        Pagamento lastPagamento = new Pagamento();
        lastPagamento.setNumeroRecibo("REC-20240315-0005");
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(anyLong(), anyInt(), anyInt()))
                .thenReturn(false);
        when(pagamentoRepository.findTopByOrderByNumeroReciboDesc()).thenReturn(Optional.of(lastPagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        
        // Act
        PagamentoResponse response = pagamentoService.registrarPagamento(pagamentoRequest);
        
        // Assert
        assertThat(response.getNumeroRecibo()).matches("REC-\\d{8}-\\d{4}");
    }
    
    @Test
    @DisplayName("Should check if payment exists for period")
    void shouldCheckIfPaymentExistsForPeriod() {
        // Arrange
        when(pagamentoRepository.existsBySocioIdAndMesAndAno(1L, 3, 2024)).thenReturn(true);
        
        // Act
        boolean exists = pagamentoService.pagamentoJaExiste(1L, 3, 2024);
        
        // Assert
        assertThat(exists).isTrue();
        verify(pagamentoRepository).existsBySocioIdAndMesAndAno(1L, 3, 2024);
    }
    
    @Test
    @DisplayName("Should find payment by ID")
    void shouldFindPaymentById() {
        // Arrange
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(pagamento));
        
        // Act
        PagamentoResponse response = pagamentoService.buscarPorId(1L);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNumeroRecibo()).isEqualTo("REC-20240315-0001");
    }
    
    @Test
    @DisplayName("Should find payment by receipt number")
    void shouldFindPaymentByReceiptNumber() {
        // Arrange
        when(pagamentoRepository.findByNumeroRecibo("REC-20240315-0001")).thenReturn(Optional.of(pagamento));
        
        // Act
        PagamentoResponse response = pagamentoService.buscarPorNumeroRecibo("REC-20240315-0001");
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNumeroRecibo()).isEqualTo("REC-20240315-0001");
    }
}
