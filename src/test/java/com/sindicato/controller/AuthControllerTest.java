package com.sindicato.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sindicato.dto.AuthResponse;
import com.sindicato.dto.LoginRequest;
import com.sindicato.exception.InvalidCredentialsException;
import com.sindicato.exception.SessionExpiredException;
import com.sindicato.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void loginWithValidCredentialsShouldReturnAuthResponse() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("admin", "password123");
        AuthResponse response = new AuthResponse(
                "access-token",
                "refresh-token",
                "admin",
                "Administrator"
        );

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.nome").value("Administrator"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void loginWithInvalidCredentialsShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Credenciais inválidas"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH001"))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    void loginWithEmptyUsernameShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithEmptyPasswordShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("admin", "");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshWithValidTokenShouldReturnNewTokens() throws Exception {
        // Given
        AuthResponse response = new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                "admin",
                "Administrator"
        );

        when(authService.refreshToken(anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer valid-refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.nome").value("Administrator"));
    }

    @Test
    void refreshWithInvalidTokenShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.refreshToken(anyString()))
                .thenThrow(new InvalidCredentialsException("Refresh token inválido ou expirado"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH001"))
                .andExpect(jsonPath("$.message").value("Refresh token inválido ou expirado"));
    }

    @Test
    void refreshWithoutAuthorizationHeaderShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithMalformedAuthorizationHeaderShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.refreshToken(null))
                .thenThrow(new InvalidCredentialsException("Token não fornecido"));

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sessionExpiredExceptionShouldReturnUnauthorizedWithCorrectCode() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("admin", "password123");

        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new SessionExpiredException("Sessão expirada"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH002"))
                .andExpect(jsonPath("$.message").value("Sessão expirada"));
    }
}
