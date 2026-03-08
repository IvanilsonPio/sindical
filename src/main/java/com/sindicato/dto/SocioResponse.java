package com.sindicato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;

/**
 * DTO for Socio response.
 */
public class SocioResponse {
    
    private Long id;
    private String nome;
    private String cpf;
    private String matricula;
    private String rg;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;
    private String profissao;
    private StatusSocio status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    // Constructors
    public SocioResponse() {
    }
    
    public SocioResponse(Socio socio) {
        this.id = socio.getId();
        this.nome = socio.getNome();
        this.cpf = socio.getCpf();
        this.matricula = socio.getMatricula();
        this.rg = socio.getRg();
        this.dataNascimento = socio.getDataNascimento();
        this.telefone = socio.getTelefone();
        this.email = socio.getEmail();
        this.endereco = socio.getEndereco();
        this.cidade = socio.getCidade();
        this.estado = socio.getEstado();
        this.cep = socio.getCep();
        this.profissao = socio.getProfissao();
        this.status = socio.getStatus();
        this.criadoEm = socio.getCriadoEm();
        this.atualizadoEm = socio.getAtualizadoEm();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getCep() {
        return cep;
    }
    
    public void setCep(String cep) {
        this.cep = cep;
    }
    
    public String getProfissao() {
        return profissao;
    }
    
    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }
    
    public StatusSocio getStatus() {
        return status;
    }
    
    public void setStatus(StatusSocio status) {
        this.status = status;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}