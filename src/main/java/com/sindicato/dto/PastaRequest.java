package com.sindicato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PastaRequest {
    
    @NotBlank(message = "Nome da pasta é obrigatório")
    @Size(max = 255, message = "Nome da pasta deve ter no máximo 255 caracteres")
    private String nome;
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;
    
    private Long pastaPaiId;
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Long getPastaPaiId() {
        return pastaPaiId;
    }
    
    public void setPastaPaiId(Long pastaPaiId) {
        this.pastaPaiId = pastaPaiId;
    }
}
