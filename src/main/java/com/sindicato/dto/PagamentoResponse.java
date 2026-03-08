package com.sindicato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sindicato.model.Pagamento;
import com.sindicato.model.StatusPagamento;

/**
 * DTO para resposta de pagamento.
 */
public class PagamentoResponse {
    
    private Long id;
    private Long socioId;
    private String socioNome;
    private String socioCpf;
    private BigDecimal valor;
    private Integer mes;
    private Integer ano;
    private LocalDate dataPagamento;
    private String numeroRecibo;
    private String caminhoRecibo;
    private String observacoes;
    private StatusPagamento status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    // Construtores
    
    public PagamentoResponse() {
    }
    
    public PagamentoResponse(Pagamento pagamento) {
        this.id = pagamento.getId();
        this.socioId = pagamento.getSocio().getId();
        this.socioNome = pagamento.getSocio().getNome();
        this.socioCpf = pagamento.getSocio().getCpf();
        this.valor = pagamento.getValor();
        this.mes = pagamento.getMes();
        this.ano = pagamento.getAno();
        this.dataPagamento = pagamento.getDataPagamento();
        this.numeroRecibo = pagamento.getNumeroRecibo();
        this.caminhoRecibo = pagamento.getCaminhoRecibo();
        this.observacoes = pagamento.getObservacoes();
        this.status = pagamento.getStatus();
        this.criadoEm = pagamento.getCriadoEm();
        this.atualizadoEm = pagamento.getAtualizadoEm();
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
    
    public String getSocioCpf() {
        return socioCpf;
    }
    
    public void setSocioCpf(String socioCpf) {
        this.socioCpf = socioCpf;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public Integer getMes() {
        return mes;
    }
    
    public void setMes(Integer mes) {
        this.mes = mes;
    }
    
    public Integer getAno() {
        return ano;
    }
    
    public void setAno(Integer ano) {
        this.ano = ano;
    }
    
    public LocalDate getDataPagamento() {
        return dataPagamento;
    }
    
    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
    
    public String getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public String getCaminhoRecibo() {
        return caminhoRecibo;
    }

    public void setCaminhoRecibo(String caminhoRecibo) {
        this.caminhoRecibo = caminhoRecibo;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public StatusPagamento getStatus() {
        return status;
    }
    
    public void setStatus(StatusPagamento status) {
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
