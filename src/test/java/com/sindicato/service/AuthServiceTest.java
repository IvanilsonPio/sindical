package com.sindicato.service;

import com.sindicato.dto.AuthResponse;
import com.sindicato.dto.LoginRequest;
import com.sindicato.exception.InvalidCredentialsException;
import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import com.sindicato.repository.UsuarioRepository;
import com.sindicato.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private UserDetails userDetails;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setPassword("$2a$10$encodedPassword");
        usuario.setNome("Administrador");
        usuario.setStatus(StatusUsuario.ATIVO);

        userDetails = new User(
            "admin",
            "$2a$10$encodedPassword",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        loginRequest = new LoginRequest("admin", "password123");
    }

    @Test
    void shouldAuthenticateWithValidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(userDetails)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.authenticate(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getNome()).isEqualTo("Administrador");
        assertThat(response.isSuccess()).isTrue();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
        verify(jwtUtil).generateRefreshToken(userDetails);
    }

    @Test
    void shouldRejectInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticate(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Credenciais inválidas");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void shouldRejectEmptyUsername() {
        // Arrange
        LoginRequest emptyUsernameRequest = new LoginRequest("", "password123");

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticate(emptyUsernameRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Username não pode estar vazio");

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldRejectEmptyPassword() {
        // Arrange
        LoginRequest emptyPasswordRequest = new LoginRequest("admin", "");

        // Act & Assert
        assertThatThrownBy(() -> authService.authenticate(emptyPasswordRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessage("Senha não pode estar vazia");

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldRefreshToken() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generateToken(userDetails)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("new-refresh-token");

        // Act
        AuthResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getUsername()).isEqualTo("admin");

        verify(jwtUtil).validateToken(refreshToken);
        verify(jwtUtil).extractUsername(refreshToken);
    }

    @Test
    void shouldRejectInvalidRefreshToken() {
        // Arrange
        String invalidRefreshToken = "invalid-refresh-token";
        when(jwtUtil.validateToken(invalidRefreshToken)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.refreshToken(invalidRefreshToken))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Refresh token inválido ou expirado");

        verify(jwtUtil).validateToken(invalidRefreshToken);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void shouldValidateToken() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // Act
        boolean isValid = authService.validateToken(token);

        // Assert
        assertThat(isValid).isTrue();
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void shouldExtractUsername() {
        // Arrange
        String token = "valid-token";
        when(jwtUtil.extractUsername(token)).thenReturn("admin");

        // Act
        String username = authService.extractUsername(token);

        // Assert
        assertThat(username).isEqualTo("admin");
        verify(jwtUtil).extractUsername(token);
    }

    @Test
    void shouldGenerateTokenForUsuario() {
        // Arrange
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("generated-token");

        // Act
        String token = authService.generateToken(usuario);

        // Assert
        assertThat(token).isEqualTo("generated-token");
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtUtil).generateToken(userDetails);
    }
}
