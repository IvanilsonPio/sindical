package com.sindicato.dto;

import java.time.LocalDateTime;

import com.sindicato.model.Arquivo;
import com.sindicato.util.ArquivoConstants;

/**
 * DTO para resposta de arquivo.
 */
public class ArquivoResponse {
    
    private Long id;
    private Long socioId;
    private String socioNome;
    private String nomeOriginal;
    private String nomeArquivo;
    private String tipoConteudo;
    private Long tamanho;
    private String tamanhoFormatado;
    private LocalDateTime criadoEm;
    
    // Construtores
    
    public ArquivoResponse() {
    }
    
    public ArquivoResponse(Arquivo arquivo) {
        this.id = arquivo.getId();
        this.socioId = arquivo.getSocio().getId();
        this.socioNome = arquivo.getSocio().getNome();
        this.nomeOriginal = arquivo.getNomeOriginal();
        this.nomeArquivo = arquivo.getNomeArquivo();
        this.tipoConteudo = arquivo.getTipoConteudo();
        this.tamanho = arquivo.getTamanho();
        this.tamanhoFormatado = ArquivoConstants.formatarTamanho(arquivo.getTamanho());
        this.criadoEm = arquivo.getCriadoEm();
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSocioId() {
        return socioId;
    }
    
    public void setSocioId(Long socioId) {
        this.socioId = socioId;
    }
    
    public String getSocioNome() {
        return socioNome;
    }
    
    public void setSocioNome(String socioNome) {
        this.socioNome = socioNome;
    }
    
    public String getNomeOriginal() {
        return nomeOriginal;
    }
    
    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }
    
    public String getNomeArquivo() {
        return nomeArquivo;
    }
    
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
    
    public String getTipoConteudo() {
        return tipoConteudo;
    }
    
    public void setTipoConteudo(String tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }
    
    public Long getTamanho() {
        return tamanho;
    }
    
    public void setTamanho(Long tamanho) {
        this.tamanho = tamanho;
    }
    
    public String getTamanhoFormatado() {
        return tamanhoFormatado;
    }
    
    public void setTamanhoFormatado(String tamanhoFormatado) {
        this.tamanhoFormatado = tamanhoFormatado;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
