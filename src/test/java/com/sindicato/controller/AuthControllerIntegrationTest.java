package com.sindicato.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sindicato.dto.LoginRequest;
import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import com.sindicato.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController with real AuthService.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        
        // Create test user
        Usuario usuario = new Usuario();
        usuario.setUsername("testadmin");
        usuario.setPassword(passwordEncoder.encode("testpass123"));
        usuario.setNome("Test Administrator");
        usuario.setStatus(StatusUsuario.ATIVO);
        usuarioRepository.save(usuario);
    }

    @Test
    void loginWithValidCredentialsShouldReturnTokens() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testadmin", "testpass123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("testadmin"))
                .andExpect(jsonPath("$.nome").value("Test Administrator"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void loginWithInvalidPasswordShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testadmin", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH001"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginWithNonExistentUserShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH001"));
    }

    @Test
    void refreshWithValidTokenShouldReturnNewTokens() throws Exception {
        // Given - First login to get a refresh token
        LoginRequest loginRequest = new LoginRequest("testadmin", "testpass123");
        
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // When & Then - Use refresh token to get new tokens
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.username").value("testadmin"))
                .andExpect(jsonPath("$.nome").value("Test Administrator"));
    }

    @Test
    void refreshWithInvalidTokenShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH001"));
    }

    @Test
    void refreshWithoutBearerPrefixShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "some-token"))
                .andExpect(status().isUnauthorized());
    }
}
