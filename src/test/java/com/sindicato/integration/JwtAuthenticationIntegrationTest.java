package com.sindicato.integration;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sindicato.dto.SocioUpdateRequest;
import com.sindicato.model.Socio;
import com.sindicato.model.SocioHistory;
import com.sindicato.model.StatusSocio;
import com.sindicato.model.Usuario;
import com.sindicato.repository.SocioHistoryRepository;
import com.sindicato.repository.SocioRepository;
import com.sindicato.repository.UsuarioRepository;
import com.sindicato.util.JwtUtil;

/**
 * Integration test to verify JWT authentication is properly integrated.
 * 
 * Tests:
 * - JWT token is sent in Authorization header
 * - Authenticated user is captured in backend
 * - Username is recorded in audit history
 * 
 * Requirements: 2.10, 5.2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("JWT Authentication Integration Tests")
class JwtAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private SocioHistoryRepository historyRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private String validJwtToken;
    private String testUsername = "admin";
    private Socio testSocio;

    @BeforeEach
    void setUp() {
        // Create test user if not exists
        if (!usuarioRepository.existsByUsername(testUsername)) {
            Usuario testUser = new Usuario();
            testUser.setUsername(testUsername);
            testUser.setPassword(passwordEncoder.encode("admin123"));
            testUser.setNome("Admin Test");
            usuarioRepository.save(testUser);
        }

        // Generate valid JWT token for test user
        UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);
        validJwtToken = jwtUtil.generateToken(userDetails);

        // Create test socio
        testSocio = new Socio();
        testSocio.setNome("João Silva");
        testSocio.setCpf("123.456.789-00");
        testSocio.setMatricula("MAT001");
        testSocio.setRg("12.345.678-9");
        testSocio.setDataNascimento(LocalDate.of(1980, 1, 1));
        testSocio.setProfissao("Agricultor");
        testSocio.setCep("12345-678");
        testSocio.setEndereco("Rua Teste, 100, Centro");
        testSocio.setCidade("São Paulo");
        testSocio.setEstado("SP");
        testSocio.setTelefone("(11) 1234-5678");
        testSocio.setEmail("joao@example.com");
        testSocio.setStatus(StatusSocio.ATIVO);
        testSocio = socioRepository.save(testSocio);
    }

    @Test
    @DisplayName("Should reject request without JWT token")
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/socios/{id}/detalhes", testSocio.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with invalid JWT token")
    void shouldRejectRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/socios/{id}/detalhes", testSocio.getId())
                .header("Authorization", "Bearer invalid_token_here")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should accept request with valid JWT token")
    void shouldAcceptRequestWithValidToken() throws Exception {
        mockMvc.perform(get("/api/socios/{id}/detalhes", testSocio.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testSocio.getId()))
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    @DisplayName("Should capture authenticated user in backend when updating socio")
    void shouldCaptureAuthenticatedUserWhenUpdating() throws Exception {
        // Prepare update request
        SocioUpdateRequest updateRequest = new SocioUpdateRequest();
        updateRequest.setNome("João Silva Atualizado");
        updateRequest.setCpf("123.456.789-00");
        updateRequest.setMatricula("MAT001");
        updateRequest.setRg("12.345.678-9");
        updateRequest.setDataNascimento(LocalDate.of(1980, 1, 1));
        updateRequest.setProfissao("Agricultor");
        updateRequest.setCep("12345-678");
        updateRequest.setEndereco("Rua Teste, 100, Centro");
        updateRequest.setCidade("São Paulo");
        updateRequest.setEstado("SP");
        updateRequest.setTelefone("(11) 1234-5678");
        updateRequest.setEmail("joao@example.com");
        updateRequest.setStatus(StatusSocio.ATIVO);

        // Perform update with JWT token
        mockMvc.perform(put("/api/socios/{id}/update", testSocio.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Silva Atualizado"));

        // Verify history was recorded with correct username
        Iterable<SocioHistory> history = historyRepository.findBySocioIdOrderByDataOperacaoDesc(testSocio.getId());
        assertThat(history).isNotEmpty();
        
        SocioHistory latestHistory = history.iterator().next();
        assertThat(latestHistory.getUsuario()).isEqualTo(testUsername);
        assertThat(latestHistory.getSocioId()).isEqualTo(testSocio.getId());
    }

    @Test
    @DisplayName("Should send JWT token in all protected endpoints")
    void shouldSendJwtTokenInAllProtectedEndpoints() throws Exception {
        // Test multiple endpoints to ensure JWT is sent consistently
        
        // GET /api/socios
        mockMvc.perform(get("/api/socios")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // GET /api/socios/{id}
        mockMvc.perform(get("/api/socios/{id}", testSocio.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // GET /api/socios/{id}/detalhes
        mockMvc.perform(get("/api/socios/{id}/detalhes", testSocio.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // GET /api/socios/{id}/historico
        mockMvc.perform(get("/api/socios/{id}/historico", testSocio.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should record username in history for all operations")
    void shouldRecordUsernameInHistoryForAllOperations() throws Exception {
        // Update socio
        SocioUpdateRequest updateRequest = new SocioUpdateRequest();
        updateRequest.setNome("João Silva Modificado");
        updateRequest.setCpf("123.456.789-00");
        updateRequest.setMatricula("MAT001");
        updateRequest.setRg("12.345.678-9");
        updateRequest.setDataNascimento(LocalDate.of(1980, 1, 1));
        updateRequest.setProfissao("Agricultor");
        updateRequest.setCep("12345-678");
        updateRequest.setEndereco("Rua Teste, 100, Centro");
        updateRequest.setCidade("São Paulo");
        updateRequest.setEstado("SP");
        updateRequest.setTelefone("(11) 1234-5678");
        updateRequest.setEmail("joao@example.com");
        updateRequest.setStatus(StatusSocio.ATIVO);

        mockMvc.perform(put("/api/socios/{id}/update", testSocio.getId())
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Verify history contains username
        Iterable<SocioHistory> history = historyRepository.findBySocioIdOrderByDataOperacaoDesc(testSocio.getId());
        
        for (SocioHistory record : history) {
            assertThat(record.getUsuario())
                    .as("History record should contain username")
                    .isNotNull()
                    .isNotEmpty()
                    .isEqualTo(testUsername);
        }
    }
}
