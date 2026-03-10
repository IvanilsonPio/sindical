package com.sindicato.model;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Entidade que representa um arquivo geral no sistema.
 * Diferente de Arquivo, não está vinculado a um sócio específico.
 * Pode estar organizado em pastas.
 */
@Entity
@Table(name = "arquivos_gerais")
public class ArquivoGeral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasta_id")
    private Pasta pasta;
    
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
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String descricao;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
    
    // Construtores
    
    public ArquivoGeral() {
    }
    
    public ArquivoGeral(String nomeOriginal, String nomeArquivo, String tipoConteudo, 
                        Long tamanho, String caminhoArquivo) {
        this.nomeOriginal = nomeOriginal;
        this.nomeArquivo = nomeArquivo;
        this.tipoConteudo = tipoConteudo;
        this.tamanho = tamanho;
        this.caminhoArquivo = caminhoArquivo;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Pasta getPasta() {
        return pasta;
    }
    
    public void setPasta(Pasta pasta) {
        this.pasta = pasta;
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
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArquivoGeral that = (ArquivoGeral) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ArquivoGeral{" +
                "id=" + id +
                ", nomeOriginal='" + nomeOriginal + '\'' +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", tamanho=" + tamanho +
                '}';
    }
}
