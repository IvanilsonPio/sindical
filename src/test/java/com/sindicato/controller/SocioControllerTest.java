package com.sindicato.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sindicato.dto.SocioRequest;
import com.sindicato.dto.SocioResponse;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.StatusSocio;
import com.sindicato.service.SocioService;

/**
 * Integration tests for SocioController.
 * Tests REST endpoints, pagination, filters, and error handling.
 * 
 * Requirements: 2.1, 2.2
 */
@WebMvcTest(SocioController.class)
@AutoConfigureMockMvc(addFilters = false)
class SocioControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private SocioService socioService;
    
    private SocioResponse socioResponse;
    private SocioRequest socioRequest;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        socioResponse = new SocioResponse();
        socioResponse.setId(1L);
        socioResponse.setNome("João Silva");
        socioResponse.setCpf("123.456.789-00");
        socioResponse.setMatricula("MAT001");
        socioResponse.setRg("12345678");
        socioResponse.setDataNascimento(LocalDate.of(1980, 1, 15));
        socioResponse.setTelefone("(11) 98765-4321");
        socioResponse.setEmail("joao@example.com");
        socioResponse.setEndereco("Rua das Flores, 123");
        socioResponse.setCidade("São Paulo");
        socioResponse.setEstado("SP");
        socioResponse.setCep("01234-567");
        socioResponse.setProfissao("Agricultor");
        socioResponse.setStatus(StatusSocio.ATIVO);
        socioResponse.setCriadoEm(LocalDateTime.now());
        socioResponse.setAtualizadoEm(LocalDateTime.now());
        
        socioRequest = new SocioRequest();
        socioRequest.setNome("João Silva");
        socioRequest.setCpf("123.456.789-00");
        socioRequest.setMatricula("MAT001");
        socioRequest.setRg("12345678");
        socioRequest.setDataNascimento(LocalDate.of(1980, 1, 15));
        socioRequest.setTelefone("(11) 98765-4321");
        socioRequest.setEmail("joao@example.com");
        socioRequest.setEndereco("Rua das Flores, 123");
        socioRequest.setCidade("São Paulo");
        socioRequest.setEstado("SP");
        socioRequest.setCep("01234-567");
        socioRequest.setProfissao("Agricultor");
    }
    
    // ========== GET /api/socios - List with pagination and filters ==========
    
    @Test
    @DisplayName("GET /api/socios - Should list all socios with pagination")
    void testListarSocios() throws Exception {
        // Arrange
        List<SocioResponse> socios = Arrays.asList(socioResponse);
        Page<SocioResponse> page = new PageImpl<>(socios, PageRequest.of(0, 20), 1);
        
        when(socioService.listarSocios(any(), any(), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].nome", is("João Silva")))
                .andExpect(jsonPath("$.content[0].cpf", is("123.456.789-00")))
                .andExpect(jsonPath("$.totalElements", is(1)));
        
        verify(socioService, times(1)).listarSocios(any(), any(), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/socios - Should filter by nome")
    void testListarSociosComFiltroNome() throws Exception {
        // Arrange
        List<SocioResponse> socios = Arrays.asList(socioResponse);
        Page<SocioResponse> page = new PageImpl<>(socios, PageRequest.of(0, 20), 1);
        
        when(socioService.listarSocios(eq("João"), any(), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios")
                .param("nome", "João")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("João Silva")));
        
        verify(socioService, times(1)).listarSocios(eq("João"), any(), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/socios - Should filter by status")
    void testListarSociosComFiltroStatus() throws Exception {
        // Arrange
        List<SocioResponse> socios = Arrays.asList(socioResponse);
        Page<SocioResponse> page = new PageImpl<>(socios, PageRequest.of(0, 20), 1);
        
        when(socioService.listarSocios(any(), eq(StatusSocio.ATIVO), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios")
                .param("status", "ATIVO")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("ATIVO")));
        
        verify(socioService, times(1)).listarSocios(any(), eq(StatusSocio.ATIVO), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/socios - Should search by multiple criteria")
    void testListarSociosComBuscaMultiCriterio() throws Exception {
        // Arrange
        List<SocioResponse> socios = Arrays.asList(socioResponse);
        Page<SocioResponse> page = new PageImpl<>(socios, PageRequest.of(0, 20), 1);
        
        when(socioService.buscarPorCriterios(eq("João"), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios")
                .param("search", "João")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        
        verify(socioService, times(1)).buscarPorCriterios(eq("João"), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/socios - Should support custom page size")
    void testListarSociosComTamanhoPaginaCustomizado() throws Exception {
        // Arrange
        SocioResponse socio2 = new SocioResponse();
        socio2.setId(2L);
        socio2.setNome("Maria Santos");
        socio2.setCpf("987.654.321-00");
        socio2.setMatricula("MAT002");
        socio2.setStatus(StatusSocio.ATIVO);
        
        List<SocioResponse> socios = Arrays.asList(socioResponse, socio2);
        Page<SocioResponse> page = new PageImpl<>(socios, PageRequest.of(0, 10), 2);
        
        when(socioService.listarSocios(any(), any(), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.size", is(10)));
        
        verify(socioService, times(1)).listarSocios(any(), any(), any(Pageable.class));
    }
    
    // ========== GET /api/socios/{id} - Get by ID ==========
    
    @Test
    @DisplayName("GET /api/socios/{id} - Should return socio by ID")
    void testBuscarSocioPorId() throws Exception {
        // Arrange
        when(socioService.buscarPorId(1L)).thenReturn(socioResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("João Silva")))
                .andExpect(jsonPath("$.cpf", is("123.456.789-00")))
                .andExpect(jsonPath("$.matricula", is("MAT001")));
        
        verify(socioService, times(1)).buscarPorId(1L);
    }
    
    @Test
    @DisplayName("GET /api/socios/{id} - Should return 404 when socio not found")
    void testBuscarSocioPorIdNaoEncontrado() throws Exception {
        // Arrange
        when(socioService.buscarPorId(999L)).thenThrow(new ResourceNotFoundException("Socio", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("BUS001")));
        
        verify(socioService, times(1)).buscarPorId(999L);
    }
    
    // ========== GET /api/socios/cpf/{cpf} - Get by CPF ==========
    
    @Test
    @DisplayName("GET /api/socios/cpf/{cpf} - Should return socio by CPF")
    void testBuscarSocioPorCpf() throws Exception {
        // Arrange
        when(socioService.buscarPorCpf("123.456.789-00")).thenReturn(socioResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/cpf/123.456.789-00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf", is("123.456.789-00")))
                .andExpect(jsonPath("$.nome", is("João Silva")));
        
        verify(socioService, times(1)).buscarPorCpf("123.456.789-00");
    }
    
    @Test
    @DisplayName("GET /api/socios/cpf/{cpf} - Should return 404 when CPF not found")
    void testBuscarSocioPorCpfNaoEncontrado() throws Exception {
        // Arrange
        when(socioService.buscarPorCpf("999.999.999-99")).thenThrow(new ResourceNotFoundException("Socio", "cpf", "999.999.999-99"));
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/cpf/999.999.999-99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("BUS001")));
        
        verify(socioService, times(1)).buscarPorCpf("999.999.999-99");
    }
    
    // ========== GET /api/socios/matricula/{matricula} - Get by Matrícula ==========
    
    @Test
    @DisplayName("GET /api/socios/matricula/{matricula} - Should return socio by matrícula")
    void testBuscarSocioPorMatricula() throws Exception {
        // Arrange
        when(socioService.buscarPorMatricula("MAT001")).thenReturn(socioResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/matricula/MAT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula", is("MAT001")))
                .andExpect(jsonPath("$.nome", is("João Silva")));
        
        verify(socioService, times(1)).buscarPorMatricula("MAT001");
    }
    
    // ========== GET /api/socios/status/{status} - Get by Status ==========
    
    @Test
    @DisplayName("GET /api/socios/status/{status} - Should return socios by status")
    void testBuscarSociosPorStatus() throws Exception {
        // Arrange
        List<SocioResponse> socios = Arrays.asList(socioResponse);
        Page<SocioResponse> page = new PageImpl<>(socios, PageRequest.of(0, 20), 1);
        
        when(socioService.buscarPorStatus(eq(StatusSocio.ATIVO), any(Pageable.class))).thenReturn(page);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/status/ATIVO")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("ATIVO")));
        
        verify(socioService, times(1)).buscarPorStatus(eq(StatusSocio.ATIVO), any(Pageable.class));
    }
    
    @Test
    @DisplayName("GET /api/socios/status/{status}/count - Should return count by status")
    void testContarSociosPorStatus() throws Exception {
        // Arrange
        when(socioService.contarPorStatus(StatusSocio.ATIVO)).thenReturn(5L);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/status/ATIVO/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(5)));
        
        verify(socioService, times(1)).contarPorStatus(StatusSocio.ATIVO);
    }
    
    // ========== POST /api/socios - Create ==========
    
    @Test
    @DisplayName("POST /api/socios - Should create new socio")
    void testCriarSocio() throws Exception {
        // Arrange
        when(socioService.criarSocio(any(SocioRequest.class))).thenReturn(socioResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("João Silva")))
                .andExpect(jsonPath("$.cpf", is("123.456.789-00")));
        
        verify(socioService, times(1)).criarSocio(any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/socios - Should return 400 when nome is missing")
    void testCriarSocioSemNome() throws Exception {
        // Arrange
        socioRequest.setNome(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VAL001")))
                .andExpect(jsonPath("$.errors.nome", notNullValue()));
        
        verify(socioService, never()).criarSocio(any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/socios - Should return 400 when CPF is missing")
    void testCriarSocioSemCpf() throws Exception {
        // Arrange
        socioRequest.setCpf(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VAL001")))
                .andExpect(jsonPath("$.errors.cpf", notNullValue()));
        
        verify(socioService, never()).criarSocio(any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/socios - Should return 400 when CPF format is invalid")
    void testCriarSocioComCpfInvalido() throws Exception {
        // Arrange
        socioRequest.setCpf("12345678900");
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VAL001")))
                .andExpect(jsonPath("$.errors.cpf", notNullValue()));
        
        verify(socioService, never()).criarSocio(any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/socios - Should return 400 when matrícula is missing")
    void testCriarSocioSemMatricula() throws Exception {
        // Arrange
        socioRequest.setMatricula(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VAL001")))
                .andExpect(jsonPath("$.errors.matricula", notNullValue()));
        
        verify(socioService, never()).criarSocio(any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/socios - Should return 400 when email format is invalid")
    void testCriarSocioComEmailInvalido() throws Exception {
        // Arrange
        socioRequest.setEmail("email-invalido");
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VAL001")))
                .andExpect(jsonPath("$.errors.email", notNullValue()));
        
        verify(socioService, never()).criarSocio(any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("POST /api/socios - Should return 409 when CPF already exists")
    void testCriarSocioComCpfDuplicado() throws Exception {
        // Arrange
        when(socioService.criarSocio(any(SocioRequest.class)))
                .thenThrow(new DuplicateEntryException("CPF já cadastrado"));
        
        // Act & Assert
        mockMvc.perform(post("/api/socios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("VAL003")))
                .andExpect(jsonPath("$.message", is("CPF já cadastrado")));
        
        verify(socioService, times(1)).criarSocio(any(SocioRequest.class));
    }
    
    // ========== PUT /api/socios/{id} - Update ==========
    
    @Test
    @DisplayName("PUT /api/socios/{id} - Should update existing socio")
    void testAtualizarSocio() throws Exception {
        // Arrange
        socioResponse.setNome("João Silva Atualizado");
        when(socioService.atualizarSocio(eq(1L), any(SocioRequest.class))).thenReturn(socioResponse);
        
        socioRequest.setNome("João Silva Atualizado");
        
        // Act & Assert
        mockMvc.perform(put("/api/socios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("João Silva Atualizado")));
        
        verify(socioService, times(1)).atualizarSocio(eq(1L), any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/socios/{id} - Should return 404 when socio not found")
    void testAtualizarSocioNaoEncontrado() throws Exception {
        // Arrange
        when(socioService.atualizarSocio(eq(999L), any(SocioRequest.class)))
                .thenThrow(new ResourceNotFoundException("Socio", "id", 999L));
        
        // Act & Assert
        mockMvc.perform(put("/api/socios/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("BUS001")));
        
        verify(socioService, times(1)).atualizarSocio(eq(999L), any(SocioRequest.class));
    }
    
    @Test
    @DisplayName("PUT /api/socios/{id} - Should return 400 when validation fails")
    void testAtualizarSocioComDadosInvalidos() throws Exception {
        // Arrange
        socioRequest.setNome("");
        
        // Act & Assert
        mockMvc.perform(put("/api/socios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VAL001")));
        
        verify(socioService, never()).atualizarSocio(any(), any());
    }
    
    // ========== DELETE /api/socios/{id} - Soft Delete ==========
    
    @Test
    @DisplayName("DELETE /api/socios/{id} - Should soft delete socio")
    void testExcluirSocio() throws Exception {
        // Arrange
        doNothing().when(socioService).excluirSocio(1L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/socios/1"))
                .andExpect(status().isNoContent());
        
        verify(socioService, times(1)).excluirSocio(1L);
    }
    
    @Test
    @DisplayName("DELETE /api/socios/{id} - Should return 404 when socio not found")
    void testExcluirSocioNaoEncontrado() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Socio", "id", 999L))
                .when(socioService).excluirSocio(999L);
        
        // Act & Assert
        mockMvc.perform(delete("/api/socios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("BUS001")));
        
        verify(socioService, times(1)).excluirSocio(999L);
    }
    
    // ========== GET /api/socios/check-cpf - Check CPF exists ==========
    
    @Test
    @DisplayName("GET /api/socios/check-cpf - Should return true when CPF exists")
    void testCheckCpfExiste() throws Exception {
        // Arrange
        when(socioService.cpfJaExiste("123.456.789-00", null)).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/check-cpf")
                .param("cpf", "123.456.789-00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(true)));
        
        verify(socioService, times(1)).cpfJaExiste("123.456.789-00", null);
    }
    
    @Test
    @DisplayName("GET /api/socios/check-cpf - Should return false when CPF does not exist")
    void testCheckCpfNaoExiste() throws Exception {
        // Arrange
        when(socioService.cpfJaExiste("999.999.999-99", null)).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/check-cpf")
                .param("cpf", "999.999.999-99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)));
        
        verify(socioService, times(1)).cpfJaExiste("999.999.999-99", null);
    }
    
    @Test
    @DisplayName("GET /api/socios/check-cpf - Should exclude ID when provided")
    void testCheckCpfComExclusaoId() throws Exception {
        // Arrange
        when(socioService.cpfJaExiste("123.456.789-00", 1L)).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/check-cpf")
                .param("cpf", "123.456.789-00")
                .param("excludeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)));
        
        verify(socioService, times(1)).cpfJaExiste("123.456.789-00", 1L);
    }
    
    // ========== GET /api/socios/check-matricula - Check Matrícula exists ==========
    
    @Test
    @DisplayName("GET /api/socios/check-matricula - Should return true when matrícula exists")
    void testCheckMatriculaExiste() throws Exception {
        // Arrange
        when(socioService.matriculaJaExiste("MAT001", null)).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/check-matricula")
                .param("matricula", "MAT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(true)));
        
        verify(socioService, times(1)).matriculaJaExiste("MAT001", null);
    }
    
    @Test
    @DisplayName("GET /api/socios/check-matricula - Should return false when matrícula does not exist")
    void testCheckMatriculaNaoExiste() throws Exception {
        // Arrange
        when(socioService.matriculaJaExiste("MAT999", null)).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/check-matricula")
                .param("matricula", "MAT999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists", is(false)));
        
        verify(socioService, times(1)).matriculaJaExiste("MAT999", null);
    }
    
    // ========== Exception Handling Tests ==========
    
    @Test
    @DisplayName("Should handle generic exceptions with 500 status")
    void testHandleGenericException() throws Exception {
        // Arrange
        when(socioService.buscarPorId(1L)).thenThrow(new RuntimeException("Unexpected error"));
        
        // Act & Assert
        mockMvc.perform(get("/api/socios/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("SYS003")))
                .andExpect(jsonPath("$.message", is("Erro interno do servidor")));
        
        verify(socioService, times(1)).buscarPorId(1L);
    }
}
