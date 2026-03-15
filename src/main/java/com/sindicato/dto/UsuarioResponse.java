package com.sindicato.dto;

import java.time.LocalDateTime;

import com.sindicato.model.Usuario;

public class UsuarioResponse {

    private Long id;
    private String username;
    private String nome;
    private String status;
    private String role;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.nome = usuario.getNome();
        this.status = usuario.getStatus().name();
        this.role = usuario.getRole().name();
        this.criadoEm = usuario.getCriadoEm();
        this.atualizadoEm = usuario.getAtualizadoEm();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getNome() { return nome; }
    public String getStatus() { return status; }
    public String getRole() { return role; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
