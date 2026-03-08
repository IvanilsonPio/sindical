package com.sindicato.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sindicato.dto.SocioRequest;
import com.sindicato.dto.SocioResponse;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.SocioRepository;

@ExtendWith(MockitoExtension.class)
class SocioServiceTest {
    
    @Mock
    private SocioRepository socioRepository;
    
    @Mock
    private SocioHistoryService historyService;
    
    @InjectMocks
    private SocioService socioService;
    
    private SocioRequest validRequest;
    private Socio existingSocio;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        validRequest = new SocioRequest();
        validRequest.setNome("João Silva");
        validRequest.setCpf("123.456.789-00");
        validRequest.setMatricula("MAT001");
        validRequest.setRg("12345678");
        validRequest.setTelefone("(11) 99999-9999");
        validRequest.setEmail("joao@email.com");
        
        existingSocio = new Socio();
        existingSocio.setId(1L);
        existingSocio.setNome("João Silva");
        existingSocio.setCpf("123.456.789-00");
        existingSocio.setMatricula("MAT001");
        existingSocio.setStatus(StatusSocio.ATIVO);
        existingSocio.setCriadoEm(LocalDateTime.now());
        existingSocio.setAtualizadoEm(LocalDateTime.now());
        
        pageable = PageRequest.of(0, 10);
    }
    
    @Test
    void criarSocio_ComDadosValidos_DeveCriarSocio() {
        when(socioRepository.existsByCpf(validRequest.getCpf())).thenReturn(false);
        when(socioRepository.existsByMatricula(validRequest.getMatricula())).thenReturn(false);
        when(socioRepository.save(any(Socio.class))).thenAnswer(invocation -> {
            Socio socio = invocation.getArgument(0);
            socio.setId(1L);
            socio.setCriadoEm(LocalDateTime.now());
            socio.setAtualizadoEm(LocalDateTime.now());
            return socio;
        });
        
        SocioResponse response = socioService.criarSocio(validRequest);
        
        assertNotNull(response);
        assertEquals(validRequest.getNome(), response.getNome());
        assertEquals(validRequest.getCpf(), response.getCpf());
        assertEquals(validRequest.getMatricula(), response.getMatricula());
        assertEquals(StatusSocio.ATIVO, response.getStatus());
        
        verify(socioRepository).existsByCpf(validRequest.getCpf());
        verify(socioRepository).existsByMatricula(validRequest.getMatricula());
        verify(socioRepository).save(any(Socio.class));
        verify(historyService).recordCreation(any(), any());
    }
    
    @Test
    void criarSocio_ComCpfDuplicado_DeveLancarExcecao() {
        when(socioRepository.existsByCpf(validRequest.getCpf())).thenReturn(true);
        
        DuplicateEntryException exception = assertThrows(
            DuplicateEntryException.class,
            () -> socioService.criarSocio(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("CPF"));
        verify(socioRepository).existsByCpf(validRequest.getCpf());
        verify(socioRepository, never()).save(any());
    }
    
    @Test
    void criarSocio_ComMatriculaDuplicada_DeveLancarExcecao() {
        when(socioRepository.existsByCpf(validRequest.getCpf())).thenReturn(false);
        when(socioRepository.existsByMatricula(validRequest.getMatricula())).thenReturn(true);
        
        DuplicateEntryException exception = assertThrows(
            DuplicateEntryException.class,
            () -> socioService.criarSocio(validRequest)
        );
        
        assertTrue(exception.getMessage().contains("Matrícula"));
        verify(socioRepository).existsByMatricula(validRequest.getMatricula());
        verify(socioRepository, never()).save(any());
    }
    
    @Test
    void buscarPorId_Existente_DeveRetornarSocio() {
        when(socioRepository.findById(1L)).thenReturn(Optional.of(existingSocio));
        
        SocioResponse response = socioService.buscarPorId(1L);
        
        assertNotNull(response);
        assertEquals(existingSocio.getId(), response.getId());
        assertEquals(existingSocio.getNome(), response.getNome());
        verify(socioRepository).findById(1L);
    }
    
    @Test
    void buscarPorId_NaoExistente_DeveLancarExcecao() {
        when(socioRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(
            ResourceNotFoundException.class,
            () -> socioService.buscarPorId(999L)
        );
        verify(socioRepository).findById(999L);
    }
    
    @Test
    void atualizarSocio_ComDadosValidos_DeveAtualizarSocio() {
        SocioRequest updateRequest = new SocioRequest();
        updateRequest.setNome("João Silva Atualizado");
        updateRequest.setCpf("123.456.789-00");
        updateRequest.setMatricula("MAT001");
        updateRequest.setTelefone("(11) 88888-8888");
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(existingSocio));
        when(socioRepository.save(any(Socio.class))).thenReturn(existingSocio);
        
        SocioResponse response = socioService.atualizarSocio(1L, updateRequest);
        
        assertNotNull(response);
        verify(socioRepository).findById(1L);
        verify(socioRepository).save(any(Socio.class));
        verify(historyService).recordUpdate(any(), any(), any());
    }
    
    @Test
    void atualizarSocio_ComCpfDuplicado_DeveLancarExcecao() {
        SocioRequest updateRequest = new SocioRequest();
        updateRequest.setNome("João Silva");
        updateRequest.setCpf("999.999.999-99");
        updateRequest.setMatricula("MAT001");
        
        when(socioRepository.findById(1L)).thenReturn(Optional.of(existingSocio));
        when(socioRepository.existsByCpfAndIdNot("999.999.999-99", 1L)).thenReturn(true);
        
        assertThrows(
            DuplicateEntryException.class,
            () -> socioService.atualizarSocio(1L, updateRequest)
        );
        verify(socioRepository, never()).save(any());
    }
    
    @Test
    void excluirSocio_Existente_DeveAlterarStatus() {
        when(socioRepository.findById(1L)).thenReturn(Optional.of(existingSocio));
        when(socioRepository.save(any(Socio.class))).thenReturn(existingSocio);
        
        socioService.excluirSocio(1L);
        
        assertEquals(StatusSocio.INATIVO, existingSocio.getStatus());
        verify(socioRepository).save(existingSocio);
        verify(historyService).recordDeletion(any(), any());
    }
    
    @Test
    void listarSocios_SemFiltros_DeveRetornarPagina() {
        Page<Socio> page = new PageImpl<>(Arrays.asList(existingSocio));
        when(socioRepository.findAll(pageable)).thenReturn(page);
        
        Page<SocioResponse> result = socioService.listarSocios(null, null, pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(existingSocio.getNome(), result.getContent().get(0).getNome());
        verify(socioRepository).findAll(pageable);
    }
    
    @Test
    void buscarPorCriterios_ComTermoValido_DeveRetornarResultados() {
        Page<Socio> page = new PageImpl<>(Arrays.asList(existingSocio));
        when(socioRepository.searchByMultipleCriteria("João", pageable)).thenReturn(page);
        
        Page<SocioResponse> result = socioService.buscarPorCriterios("João", pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(socioRepository).searchByMultipleCriteria("João", pageable);
    }
    
    @Test
    void cpfJaExiste_Existente_DeveRetornarTrue() {
        when(socioRepository.existsByCpf("123.456.789-00")).thenReturn(true);
        
        boolean result = socioService.cpfJaExiste("123.456.789-00", null);
        
        assertTrue(result);
    }
    
    @Test
    void cpfJaExiste_NaoExistente_DeveRetornarFalse() {
        when(socioRepository.existsByCpf("999.999.999-99")).thenReturn(false);
        
        boolean result = socioService.cpfJaExiste("999.999.999-99", null);
        
        assertFalse(result);
    }
}
