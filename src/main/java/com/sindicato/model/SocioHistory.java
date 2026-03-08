package com.sindicato.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity for tracking changes to Socio entities.
 * Implements change history as required by Requisito 2.3.
 */
@Entity
@Table(name = "socio_history")
public class SocioHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "socio_id", nullable = false)
    private Long socioId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacao", nullable = false, length = 20)
    private TipoOperacao tipoOperacao;
    
    @Column(name = "dados_anteriores", columnDefinition = "TEXT")
    private String dadosAnteriores;
    
    @Column(name = "dados_novos", columnDefinition = "TEXT")
    private String dadosNovos;
    
    @Column(length = 100)
    private String usuario;
    
    @CreationTimestamp
    @Column(name = "data_operacao", nullable = false, updatable = false)
    private LocalDateTime dataOperacao;
    
    // Constructors
    public SocioHistory() {
    }
    
    public SocioHistory(Long socioId, TipoOperacao tipoOperacao, String dadosAnteriores, 
                       String dadosNovos, String usuario) {
        this.socioId = socioId;
        this.tipoOperacao = tipoOperacao;
        this.dadosAnteriores = dadosAnteriores;
        this.dadosNovos = dadosNovos;
        this.usuario = usuario;
    }
    
    // Getters and Setters
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
    
    public TipoOperacao getTipoOperacao() {
        return tipoOperacao;
    }
    
    public void setTipoOperacao(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
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
    
    public String getUsuario() {
        return usuario;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    public LocalDateTime getDataOperacao() {
        return dataOperacao;
    }
    
    public void setDataOperacao(LocalDateTime dataOperacao) {
        this.dataOperacao = dataOperacao;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocioHistory that = (SocioHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "SocioHistory{" +
                "id=" + id +
                ", socioId=" + socioId +
                ", tipoOperacao=" + tipoOperacao +
                ", usuario='" + usuario + '\'' +
                ", dataOperacao=" + dataOperacao +
                '}';
    }
}