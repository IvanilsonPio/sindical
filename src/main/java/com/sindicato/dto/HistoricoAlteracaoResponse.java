package com.sindicato.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for history of alterations response.
 * Contains information about changes made to a Socio entity.
 */
public class HistoricoAlteracaoResponse {
    
    private Long id;
    private Long socioId;
    private String usuario;
    private LocalDateTime dataHora;
    private String operacao;
    private Map<String, CampoAlterado> camposAlterados;
    
    // Constructors
    
    public HistoricoAlteracaoResponse() {
    }
    
    public HistoricoAlteracaoResponse(Long id, Long socioId, String usuario, 
                                     LocalDateTime dataHora, String operacao, 
                                     Map<String, CampoAlterado> camposAlterados) {
        this.id = id;
        this.socioId = socioId;
        this.usuario = usuario;
        this.dataHora = dataHora;
        this.operacao = operacao;
        this.camposAlterados = camposAlterados;
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
    
    public String getUsuario() {
        return usuario;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public String getOperacao() {
        return operacao;
    }
    
    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }
    
    public Map<String, CampoAlterado> getCamposAlterados() {
        return camposAlterados;
    }
    
    public void setCamposAlterados(Map<String, CampoAlterado> camposAlterados) {
        this.camposAlterados = camposAlterados;
    }
}
