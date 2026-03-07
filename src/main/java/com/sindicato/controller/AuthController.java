package com.sindicato.controller;

import com.sindicato.dto.AuthResponse;
import com.sindicato.dto.LoginRequest;
import com.sindicato.exception.InvalidCredentialsException;
import com.sindicato.exception.SessionExpiredException;
import com.sindicato.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Provides endpoints for login and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates user with provided credentials.
     *
     * @param request login request containing username and password
     * @return authentication response with tokens and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            throw e;
        }
    }

    /**
     * Refreshes access token using refresh token.
     *
     * @param authorizationHeader Authorization header containing Bearer token
     * @return authentication response with new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract token from Bearer header
            String token = extractTokenFromHeader(authorizationHeader);
            
            if (token == null) {
                throw new InvalidCredentialsException("Token não fornecido");
            }

            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            throw e;
        }
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * @param authorizationHeader Authorization header value
     * @return extracted token or null if header is invalid
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * Exception handler for InvalidCredentialsException.
     *
     * @param e exception
     * @return error response
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        ErrorResponse error = new ErrorResponse("AUTH001", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Exception handler for SessionExpiredException.
     *
     * @param e exception
     * @return error response
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpired(SessionExpiredException e) {
        ErrorResponse error = new ErrorResponse("AUTH002", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * DTO for error responses.
     */
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
