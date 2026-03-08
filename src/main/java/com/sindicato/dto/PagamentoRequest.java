package com.sindicato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de criação/atualização de pagamento.
 */
public class PagamentoRequest {
    
    @NotNull(message = "ID do sócio é obrigatório")
    private Long socioId;
    
    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;
    
    @NotNull(message = "Mês é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    private Integer mes;
    
    @NotNull(message = "Ano é obrigatório")
    @Min(value = 2020, message = "Ano deve ser maior ou igual a 2020")
    private Integer ano;
    
    @NotNull(message = "Data de pagamento é obrigatória")
    private LocalDate dataPagamento;
    
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
    
    // Construtores
    
    public PagamentoRequest() {
    }
    
    public PagamentoRequest(Long socioId, BigDecimal valor, Integer mes, Integer ano, LocalDate dataPagamento) {
        this.socioId = socioId;
        this.valor = valor;
        this.mes = mes;
        this.ano = ano;
        this.dataPagamento = dataPagamento;
    }
    
    // Getters e Setters
    
    public Long getSocioId() {
        return socioId;
    }
    
    public void setSocioId(Long socioId) {
        this.socioId = socioId;
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
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
