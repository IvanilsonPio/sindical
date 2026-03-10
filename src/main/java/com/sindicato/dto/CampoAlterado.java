package com.sindicato.dto;

/**
 * DTO representing a changed field in the history of alterations.
 * Contains the field name and its previous and new values.
 */
public class CampoAlterado {
    
    private String nomeCampo;
    private String valorAnterior;
    private String valorNovo;
    
    // Constructors
    
    public CampoAlterado() {
    }
    
    public CampoAlterado(String nomeCampo, String valorAnterior, String valorNovo) {
        this.nomeCampo = nomeCampo;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
    }
    
    // Getters and Setters
    
    public String getNomeCampo() {
        return nomeCampo;
    }
    
    public void setNomeCampo(String nomeCampo) {
        this.nomeCampo = nomeCampo;
    }
    
    public String getValorAnterior() {
        return valorAnterior;
    }
    
    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }
    
    public String getValorNovo() {
        return valorNovo;
    }
    
    public void setValorNovo(String valorNovo) {
        this.valorNovo = valorNovo;
    }
}
