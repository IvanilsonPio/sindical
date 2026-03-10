package com.sindicato.dto;

import java.time.LocalDateTime;

import com.sindicato.model.ArquivoGeral;
import com.sindicato.util.ArquivoConstants;

public class ArquivoGeralResponse {
    
    private Long id;
    private String nomeOriginal;
    private String tipoConteudo;
    private Long tamanho;
    private String tamanhoFormatado;
    private String descricao;
    private Long pastaId;
    private String pastaNome;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    public ArquivoGeralResponse(ArquivoGeral arquivo) {
        this.id = arquivo.getId();
        this.nomeOriginal = arquivo.getNomeOriginal();
        this.tipoConteudo = arquivo.getTipoConteudo();
        this.tamanho = arquivo.getTamanho();
        this.tamanhoFormatado = ArquivoConstants.formatarTamanho(arquivo.getTamanho());
        this.descricao = arquivo.getDescricao();
        this.pastaId = arquivo.getPasta() != null ? arquivo.getPasta().getId() : null;
        this.pastaNome = arquivo.getPasta() != null ? arquivo.getPasta().getNome() : null;
        this.criadoEm = arquivo.getCriadoEm();
        this.atualizadoEm = arquivo.getAtualizadoEm();
    }
    
    public Long getId() {
        return id;
    }
    
    public String getNomeOriginal() {
        return nomeOriginal;
    }
    
    public String getTipoConteudo() {
        return tipoConteudo;
    }
    
    public Long getTamanho() {
        return tamanho;
    }
    
    public String getTamanhoFormatado() {
        return tamanhoFormatado;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public Long getPastaId() {
        return pastaId;
    }
    
    public String getPastaNome() {
        return pastaNome;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
