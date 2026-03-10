package com.sindicato.dto;

import java.time.LocalDate;

import com.sindicato.model.StatusSocio;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de atualização de sócio.
 * Contém validações Bean Validation para todos os campos.
 * 
 * Requirements: 2.3, 2.4, 2.5, 2.6, 2.7
 */
public class SocioUpdateRequest {
    
    // Dados pessoais obrigatórios
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;
    
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", 
             message = "CPF deve estar no formato XXX.XXX.XXX-XX")
    private String cpf;
    
    @NotBlank(message = "Matrícula é obrigatória")
    @Size(max = 20, message = "Matrícula deve ter no máximo 20 caracteres")
    private String matricula;
    
    // Dados pessoais opcionais
    
    @Size(max = 20, message = "RG deve ter no máximo 20 caracteres")
    private String rg;
    
    private LocalDate dataNascimento;
    
    @Size(max = 50, message = "Profissão deve ter no máximo 50 caracteres")
    private String profissao;
    
    // Endereço
    
    @Pattern(regexp = "\\d{5}-\\d{3}|\\d{8}", 
             message = "CEP deve estar no formato XXXXX-XXX ou conter 8 dígitos")
    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    private String cep;
    
    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String endereco;
    
    @Size(max = 50, message = "Cidade deve ter no máximo 50 caracteres")
    private String cidade;
    
    @Size(max = 2, message = "Estado deve ter 2 caracteres")
    @Pattern(regexp = "[A-Z]{2}", 
             message = "Estado deve conter 2 letras maiúsculas")
    private String estado;
    
    // Contato
    
    @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}", 
             message = "Telefone deve estar no formato (XX) XXXXX-XXXX ou (XX) XXXX-XXXX")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;
    
    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;
    
    // Status
    
    @NotNull(message = "Status é obrigatório")
    private StatusSocio status;
    
    // Construtores
    
    public SocioUpdateRequest() {
    }
    
    // Getters e Setters
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getMatricula() {
        return matricula;
    }
    
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
    
    public String getRg() {
        return rg;
    }
    
    public void setRg(String rg) {
        this.rg = rg;
    }
    
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
    
    public String getProfissao() {
        return profissao;
    }
    
    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }
    
    public String getCep() {
        return cep;
    }
    
    public void setCep(String cep) {
        this.cep = cep;
    }
    
    public String getEndereco() {
        return endereco;
    }
    
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    
    public String getCidade() {
        return cidade;
    }
    
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public StatusSocio getStatus() {
        return status;
    }
    
    public void setStatus(StatusSocio status) {
        this.status = status;
    }
}
