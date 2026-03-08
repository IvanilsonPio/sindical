package com.sindicato.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ActivityReportResponse {
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFim;
    private Long totalOperacoes;
    private Map<String, Long> operacoesPorEntidade;
    private Map<String, Long> operacoesPorTipo;
    private Map<String, Long> operacoesPorUsuario;
    private Long totalSociosCriados;
    private Long totalPagamentosRegistrados;
    private Long totalArquivosUpload;
    
    // Constructors
    public ActivityReportResponse() {
    }
    
    // Getters and Setters
    public LocalDateTime getPeriodoInicio() {
        return periodoInicio;
    }
    
    public void setPeriodoInicio(LocalDateTime periodoInicio) {
        this.periodoInicio = periodoInicio;
    }
    
    public LocalDateTime getPeriodoFim() {
        return periodoFim;
    }
    
    public void setPeriodoFim(LocalDateTime periodoFim) {
        this.periodoFim = periodoFim;
    }
    
    public Long getTotalOperacoes() {
        return totalOperacoes;
    }
    
    public void setTotalOperacoes(Long totalOperacoes) {
        this.totalOperacoes = totalOperacoes;
    }
    
    public Map<String, Long> getOperacoesPorEntidade() {
        return operacoesPorEntidade;
    }
    
    public void setOperacoesPorEntidade(Map<String, Long> operacoesPorEntidade) {
        this.operacoesPorEntidade = operacoesPorEntidade;
    }
    
    public Map<String, Long> getOperacoesPorTipo() {
        return operacoesPorTipo;
    }
    
    public void setOperacoesPorTipo(Map<String, Long> operacoesPorTipo) {
        this.operacoesPorTipo = operacoesPorTipo;
    }
    
    public Map<String, Long> getOperacoesPorUsuario() {
        return operacoesPorUsuario;
    }
    
    public void setOperacoesPorUsuario(Map<String, Long> operacoesPorUsuario) {
        this.operacoesPorUsuario = operacoesPorUsuario;
    }
    
    public Long getTotalSociosCriados() {
        return totalSociosCriados;
    }
    
    public void setTotalSociosCriados(Long totalSociosCriados) {
        this.totalSociosCriados = totalSociosCriados;
    }
    
    public Long getTotalPagamentosRegistrados() {
        return totalPagamentosRegistrados;
    }
    
    public void setTotalPagamentosRegistrados(Long totalPagamentosRegistrados) {
        this.totalPagamentosRegistrados = totalPagamentosRegistrados;
    }
    
    public Long getTotalArquivosUpload() {
        return totalArquivosUpload;
    }
    
    public void setTotalArquivosUpload(Long totalArquivosUpload) {
        this.totalArquivosUpload = totalArquivosUpload;
    }
}
