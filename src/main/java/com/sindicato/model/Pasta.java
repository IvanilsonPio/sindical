package com.sindicato.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa uma pasta no sistema de arquivos.
 * Suporta hierarquia de pastas (pasta pai/filha).
 */
@Entity
@Table(name = "pastas")
public class Pasta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da pasta é obrigatório")
    @Size(max = 255, message = "Nome da pasta deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String nome;
    
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(length = 500)
    private String descricao;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasta_pai_id")
    private Pasta pastaPai;
    
    @OneToMany(mappedBy = "pastaPai", cascade = CascadeType.ALL)
    private List<Pasta> subpastas = new ArrayList<>();
    
    @OneToMany(mappedBy = "pasta", cascade = CascadeType.ALL)
    private List<ArquivoGeral> arquivos = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
    
    // Construtores
    
    public Pasta() {
    }
    
    public Pasta(String nome) {
        this.nome = nome;
    }
    
    public Pasta(String nome, Pasta pastaPai) {
        this.nome = nome;
        this.pastaPai = pastaPai;
    }
    
    // Getters e Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Pasta getPastaPai() {
        return pastaPai;
    }
    
    public void setPastaPai(Pasta pastaPai) {
        this.pastaPai = pastaPai;
    }
    
    public List<Pasta> getSubpastas() {
        return subpastas;
    }
    
    public void setSubpastas(List<Pasta> subpastas) {
        this.subpastas = subpastas;
    }
    
    public List<ArquivoGeral> getArquivos() {
        return arquivos;
    }
    
    public void setArquivos(List<ArquivoGeral> arquivos) {
        this.arquivos = arquivos;
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
    
    // Métodos auxiliares
    
    public void addSubpasta(Pasta subpasta) {
        subpastas.add(subpasta);
        subpasta.setPastaPai(this);
    }
    
    public void removeSubpasta(Pasta subpasta) {
        subpastas.remove(subpasta);
        subpasta.setPastaPai(null);
    }
    
    public void addArquivo(ArquivoGeral arquivo) {
        arquivos.add(arquivo);
        arquivo.setPasta(this);
    }
    
    public void removeArquivo(ArquivoGeral arquivo) {
        arquivos.remove(arquivo);
        arquivo.setPasta(null);
    }
    
    /**
     * Retorna o caminho completo da pasta (ex: /Documentos/2024/Janeiro)
     */
    public String getCaminhoCompleto() {
        if (pastaPai == null) {
            return "/" + nome;
        }
        return pastaPai.getCaminhoCompleto() + "/" + nome;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pasta pasta = (Pasta) o;
        return Objects.equals(id, pasta.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Pasta{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", caminhoCompleto='" + getCaminhoCompleto() + '\'' +
                '}';
    }
}
