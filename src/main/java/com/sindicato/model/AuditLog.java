package com.sindicato.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String entidade;
    
    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoOperacao operacao;
    
    @Column(length = 100)
    private String usuario;
    
    @Column(name = "dados_anteriores", columnDefinition = "TEXT")
    private String dadosAnteriores;
    
    @Column(name = "dados_novos", columnDefinition = "TEXT")
    private String dadosNovos;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
    
    // Constructors
    public AuditLog() {
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
