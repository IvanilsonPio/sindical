package com.sindicato.service;

import com.sindicato.dto.AuthResponse;
import com.sindicato.dto.LoginRequest;
import com.sindicato.exception.InvalidCredentialsException;
import com.sindicato.model.Usuario;
import com.sindicato.repository.UsuarioRepository;
import com.sindicato.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication operations including login, token generation, and validation.
 * Implements JWT-based authentication with access and refresh tokens.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager,
                      CustomUserDetailsService userDetailsService,
                      UsuarioRepository usuarioRepository,
                      JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates user with provided credentials and generates JWT tokens.
     *
     * @param request login request containing username and password
     * @return authentication response with tokens and user information
     * @throws InvalidCredentialsException if credentials are invalid
     */
    public AuthResponse authenticate(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new InvalidCredentialsException("Username não pode estar vazio");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new InvalidCredentialsException("Senha não pode estar vazia");
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            
            // Get user entity for additional information
            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            // Generate tokens
            String token = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return new AuthResponse(token, refreshToken, usuario.getUsername(), usuario.getNome());

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }
    }

    /**
     * Generates new access token from refresh token.
     *
     * @param refreshToken refresh token
     * @return authentication response with new tokens
     * @throws InvalidCredentialsException if refresh token is invalid
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new InvalidCredentialsException("Refresh token inválido ou expirado");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

            String newToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            return new AuthResponse(newToken, newRefreshToken, usuario.getUsername(), usuario.getNome());

        } catch (Exception e) {
            throw new InvalidCredentialsException("Erro ao renovar token: " + e.getMessage());
        }
    }

    /**
     * Validates JWT token.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * Extracts username from JWT token.
     *
     * @param token JWT token
     * @return username extracted from token
     */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    /**
     * Generates JWT token for user.
     *
     * @param usuario user entity
     * @return generated JWT token
     */
    public String generateToken(Usuario usuario) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        return jwtUtil.generateToken(userDetails);
    }
}
