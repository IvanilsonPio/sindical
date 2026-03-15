package com.sindicato.dto;

/**
 * DTO for authentication response containing tokens and user information.
 */
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private String nome;
    private String role;
    private boolean success;

    public AuthResponse() {
    }

    public AuthResponse(String token, String refreshToken, String username, String nome, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.nome = nome;
        this.role = role;
        this.success = true;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
