package com.sindicato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sindicato.model.Pasta;

@Repository
public interface PastaRepository extends JpaRepository<Pasta, Long> {
    
    /**
     * Busca todas as pastas raiz (sem pasta pai)
     */
    List<Pasta> findByPastaPaiIsNull();
    
    /**
     * Busca todas as subpastas de uma pasta específica
     */
    List<Pasta> findByPastaPaiId(Long pastaPaiId);
    
    /**
     * Busca pasta por nome e pasta pai
     */
    @Query("SELECT p FROM Pasta p WHERE p.nome = :nome AND p.pastaPai.id = :pastaPaiId")
    List<Pasta> findByNomeAndPastaPaiId(@Param("nome") String nome, @Param("pastaPaiId") Long pastaPaiId);
    
    /**
     * Busca pasta raiz por nome
     */
    List<Pasta> findByNomeAndPastaPaiIsNull(String nome);
    
    /**
     * Conta subpastas de uma pasta
     */
    @Query("SELECT COUNT(p) FROM Pasta p WHERE p.pastaPai.id = :pastaId")
    long countSubpastasByPastaId(@Param("pastaId") Long pastaId);
    
    /**
     * Conta arquivos de uma pasta
     */
    @Query("SELECT COUNT(a) FROM ArquivoGeral a WHERE a.pasta.id = :pastaId")
    long countArquivosByPastaId(@Param("pastaId") Long pastaId);
}
