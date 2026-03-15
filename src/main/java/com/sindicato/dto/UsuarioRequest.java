package com.sindicato.dto;

import com.sindicato.model.RoleUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioRequest {

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotNull(message = "Perfil é obrigatório")
    private RoleUsuario role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RoleUsuario getRole() { return role; }
    public void setRole(RoleUsuario role) { this.role = role; }
}
