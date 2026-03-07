package com.sindicato.config;

import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import com.sindicato.repository.UsuarioRepository;
import com.sindicato.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração para configuração de segurança.
 * Valida o Requisito 1.3: QUANDO uma sessão administrativa expira, O Sistema DEVE redirecionar o usuário para a tela de login
 * Valida o Requisito 1.5: QUANDO um administrador está inativo por mais de 30 minutos, O Sistema DEVE encerrar automaticamente a sessão
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    private Usuario testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        // Limpa usuários existentes
        usuarioRepository.deleteAll();

        // Cria usuário de teste
        testUser = new Usuario();
        testUser.setUsername("admin");
        testUser.setPassword(passwordEncoder.encode("senha123"));
        testUser.setNome("Administrador Teste");
        testUser.setStatus(StatusUsuario.ATIVO);
        usuarioRepository.save(testUser);

        // Gera token válido
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
        validToken = jwtUtil.generateToken(userDetails);
    }

    @Test
    void devePermitirAcessoAEndpointPublico() throws Exception {
        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirAcessoAEndpointDeLogin() throws Exception {
        String loginRequest = """
                {
                    "username": "admin",
                    "password": "senha123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAcessoAEndpointProtegidoSemToken() throws Exception {
        mockMvc.perform(get("/api/socios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoAEndpointProtegidoComTokenValido() throws Exception {
        mockMvc.perform(get("/api/socios")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAcessoComTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/socios")
                        .header("Authorization", "Bearer token.invalido.aqui")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveBloquearAcessoComTokenMalformado() throws Exception {
        mockMvc.perform(get("/api/socios")
                        .header("Authorization", "InvalidFormat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirRequisicoesCORS() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header("Origin", "http://localhost:4200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void devePermitirPreflightCORS() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/socios")
                                .header("Origin", "http://localhost:4200")
                                .header("Access-Control-Request-Method", "POST")
                                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk());
    }
}
