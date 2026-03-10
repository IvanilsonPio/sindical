package com.sindicato.dto;

import java.time.LocalDateTime;

import com.sindicato.model.Pasta;

public class PastaResponse {
    
    private Long id;
    private String nome;
    private String descricao;
    private String caminhoCompleto;
    private Long pastaPaiId;
    private int quantidadeSubpastas;
    private int quantidadeArquivos;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    public PastaResponse(Pasta pasta, long quantidadeSubpastas, long quantidadeArquivos) {
        this.id = pasta.getId();
        this.nome = pasta.getNome();
        this.descricao = pasta.getDescricao();
        this.caminhoCompleto = pasta.getCaminhoCompleto();
        this.pastaPaiId = pasta.getPastaPai() != null ? pasta.getPastaPai().getId() : null;
        this.quantidadeSubpastas = (int) quantidadeSubpastas;
        this.quantidadeArquivos = (int) quantidadeArquivos;
        this.criadoEm = pasta.getCriadoEm();
        this.atualizadoEm = pasta.getAtualizadoEm();
    }
    
    public Long getId() {
        return id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public String getCaminhoCompleto() {
        return caminhoCompleto;
    }
    
    public Long getPastaPaiId() {
        return pastaPaiId;
    }
    
    public int getQuantidadeSubpastas() {
        return quantidadeSubpastas;
    }
    
    public int getQuantidadeArquivos() {
        return quantidadeArquivos;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
