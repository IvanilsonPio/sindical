package com.sindicato.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entidade que representa um pagamento mensal de um sócio.
 * Contém informações sobre o valor pago, período (mês/ano), data de pagamento e número do recibo.
 * A constraint única (socio_id, mes, ano) garante que não haja pagamentos duplicados para o mesmo período.
 */
@Entity
@Table(name = "pagamentos", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pagamento_socio_periodo", columnNames = {"socio_id", "mes", "ano"})
})
public class Pagamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Sócio é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;
    
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @NotNull(message = "Mês é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    @Column(nullable = false)
    private Integer mes;
    
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2020, message = "Ano deve ser maior ou igual a 2020")
    @Column(nullable = false)
    private Integer ano;
    
    @NotNull(message = "Data de pagamento é obrigatória")
    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;
    
    @NotBlank(message = "Número do recibo é obrigatório")
    @Size(max = 20, message = "Número do recibo deve ter no máximo 20 caracteres")
    @Column(name = "numero_recibo", unique = true, nullable = false, length = 20)
    private String numeroRecibo;
    
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status = StatusPagamento.PAGO;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
    
    // Construtores
    
    public Pagamento() {
    }
    
    public Pagamento(Socio socio, BigDecimal valor, Integer mes, Integer ano, LocalDate dataPagamento, String numeroRecibo) {
        this.socio = socio;
        this.valor = valor;
        this.mes = mes;
        this.ano = ano;
        this.dataPagamento = dataPagamento;
        this.numeroRecibo = numeroRecibo;
        this.status = StatusPagamento.PAGO;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Socio getSocio() {
        return socio;
    }
    
    public void setSocio(Socio socio) {
        this.socio = socio;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public int getMes() {
        return mes;
    }
    
    public void setMes(Integer mes) {
        this.mes = mes;
    }
    
    public int getAno() {
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

    public String getCaminhoRecibo() {
        return caminhoRecibo;
    }

    public void setCaminhoRecibo(String caminhoRecibo) {
        this.caminhoRecibo = caminhoRecibo;
    }

    @Column(name = "caminho_recibo", length = 500)
    private String caminhoRecibo;
    
    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
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
    
    // equals, hashCode e toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pagamento pagamento = (Pagamento) o;
        return Objects.equals(id, pagamento.id) && 
               Objects.equals(numeroRecibo, pagamento.numeroRecibo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, numeroRecibo);
    }
    
    @Override
    public String toString() {
        return "Pagamento{" +
                "id=" + id +
                ", socioId=" + (socio != null ? socio.getId() : null) +
                ", valor=" + valor +
                ", mes=" + mes +
                ", ano=" + ano +
                ", dataPagamento=" + dataPagamento +
                ", numeroRecibo='" + numeroRecibo + '\'' +
                ", status=" + status +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                '}';
    }
}
