package com.sindicato.dto;

import java.time.LocalDateTime;

import com.sindicato.model.AuditLog;
import com.sindicato.model.TipoOperacao;

public class AuditLogResponse {
    private Long id;
    private String entidade;
    private Long entidadeId;
    private TipoOperacao operacao;
    private String usuario;
    private String dadosAnteriores;
    private String dadosNovos;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime criadoEm;
    
    // Constructors
    public AuditLogResponse() {
    }
    
    public AuditLogResponse(AuditLog auditLog) {
        this.id = auditLog.getId();
        this.entidade = auditLog.getEntidade();
        this.entidadeId = auditLog.getEntidadeId();
        this.operacao = auditLog.getOperacao();
        this.usuario = auditLog.getUsuario();
        this.dadosAnteriores = auditLog.getDadosAnteriores();
        this.dadosNovos = auditLog.getDadosNovos();
        this.ipAddress = auditLog.getIpAddress();
        this.userAgent = auditLog.getUserAgent();
        this.criadoEm = auditLog.getCriadoEm();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEntidade() {
        return entidade;
    }
    
    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }
    
    public Long getEntidadeId() {
        return entidadeId;
    }
    
    public void setEntidadeId(Long entidadeId) {
        this.entidadeId = entidadeId;
    }
    
    public TipoOperacao getOperacao() {
        return operacao;
    }
    
    public void setOperacao(TipoOperacao operacao) {
        this.operacao = operacao;
    }
    
    public String getUsuario() {
        return usuario;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    public String getDadosAnteriores() {
        return dadosAnteriores;
    }
    
    public void setDadosAnteriores(String dadosAnteriores) {
        this.dadosAnteriores = dadosAnteriores;
    }
    
    public String getDadosNovos() {
        return dadosNovos;
    }
    
    public void setDadosNovos(String dadosNovos) {
        this.dadosNovos = dadosNovos;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
