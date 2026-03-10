package com.sindicato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sindicato.model.ArquivoGeral;

@Repository
public interface ArquivoGeralRepository extends JpaRepository<ArquivoGeral, Long> {
    
    /**
     * Busca arquivos de uma pasta específica
     */
    @Query("SELECT a FROM ArquivoGeral a LEFT JOIN FETCH a.pasta WHERE a.pasta.id = :pastaId")
    List<ArquivoGeral> findByPastaId(@Param("pastaId") Long pastaId);
    
    /**
     * Busca arquivos sem pasta (raiz)
     */
    List<ArquivoGeral> findByPastaIsNull();
    
    /**
     * Conta arquivos de uma pasta
     */
    long countByPastaId(Long pastaId);
    
    /**
     * Soma tamanho total dos arquivos de uma pasta
     */
    @Query("SELECT COALESCE(SUM(a.tamanho), 0) FROM ArquivoGeral a WHERE a.pasta.id = :pastaId")
    Long sumTamanhoByPastaId(@Param("pastaId") Long pastaId);
    
    /**
     * Soma tamanho total de todos os arquivos gerais
     */
    @Query("SELECT COALESCE(SUM(a.tamanho), 0) FROM ArquivoGeral a")
    Long sumTamanhoTotal();
}
