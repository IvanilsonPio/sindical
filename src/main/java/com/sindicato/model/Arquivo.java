package com.sindicato.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entidade que representa um arquivo digital associado a um sócio.
 * Armazena metadados do arquivo e referência ao sócio proprietário.
 */
@Entity
@Table(name = "arquivos")
public class Arquivo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Sócio é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;
    
    @NotBlank(message = "Nome original do arquivo é obrigatório")
    @Size(max = 255, message = "Nome original deve ter no máximo 255 caracteres")
    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;
    
    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 255, message = "Nome do arquivo deve ter no máximo 255 caracteres")
    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;
    
    @NotBlank(message = "Tipo de conteúdo é obrigatório")
    @Size(max = 100, message = "Tipo de conteúdo deve ter no máximo 100 caracteres")
    @Column(name = "tipo_conteudo", nullable = false, length = 100)
    private String tipoConteudo;
    
    @NotNull(message = "Tamanho do arquivo é obrigatório")
    @Positive(message = "Tamanho do arquivo deve ser positivo")
    @Column(nullable = false)
    private Long tamanho;
    
    @NotBlank(message = "Caminho do arquivo é obrigatório")
    @Size(max = 500, message = "Caminho do arquivo deve ter no máximo 500 caracteres")
    @Column(name = "caminho_arquivo", nullable = false, length = 500)
    private String caminhoArquivo;
    
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    // Construtores
    
    public Arquivo() {
    }
    
    public Arquivo(Socio socio, String nomeOriginal, String nomeArquivo, 
                   String tipoConteudo, Long tamanho, String caminhoArquivo) {
        this.socio = socio;
        this.nomeOriginal = nomeOriginal;
        this.nomeArquivo = nomeArquivo;
        this.tipoConteudo = tipoConteudo;
        this.tamanho = tamanho;
        this.caminhoArquivo = caminhoArquivo;
    }
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
    
    // Getters e Setters básicos
    
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
    
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }
    
    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    // equals, hashCode e toString
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arquivo arquivo = (Arquivo) o;
        return Objects.equals(id, arquivo.id) && 
               Objects.equals(nomeArquivo, arquivo.nomeArquivo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nomeArquivo);
    }
    
    @Override
    public String toString() {
        return "Arquivo{" +
                "id=" + id +
                ", nomeOriginal='" + nomeOriginal + '\'' +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", tipoConteudo='" + tipoConteudo + '\'' +
                ", tamanho=" + tamanho +
                ", criadoEm=" + criadoEm +
                '}';
    }
}
