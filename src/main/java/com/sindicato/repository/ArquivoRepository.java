package com.sindicato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sindicato.model.Arquivo;

/**
 * Repositório para operações de persistência da entidade Arquivo.
 * Fornece métodos de busca customizados para consultas de arquivos associados a sócios.
 */
@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {
    
    /**
     * Busca todos os arquivos associados a um sócio específico.
     * Utiliza JOIN FETCH para carregar o sócio junto, evitando LazyInitializationException.
     * 
     * @param socioId ID do sócio
     * @return Lista de arquivos do sócio ordenados por data de criação (mais recentes primeiro)
     */
    @Query("SELECT a FROM Arquivo a JOIN FETCH a.socio WHERE a.socio.id = :socioId ORDER BY a.criadoEm DESC")
    List<Arquivo> findBySocioId(@Param("socioId") Long socioId);
    
    /**
     * Busca arquivos por tipo de conteúdo.
     * 
     * @param tipoConteudo Tipo MIME do arquivo (ex: application/pdf, image/jpeg)
     * @return Lista de arquivos do tipo especificado
     */
    List<Arquivo> findByTipoConteudo(String tipoConteudo);
    
    /**
     * Busca arquivos de um sócio por tipo de conteúdo.
     * 
     * @param socioId ID do sócio
     * @param tipoConteudo Tipo MIME do arquivo
     * @return Lista de arquivos do sócio com o tipo especificado
     */
    @Query("SELECT a FROM Arquivo a WHERE a.socio.id = :socioId AND a.tipoConteudo = :tipoConteudo ORDER BY a.criadoEm DESC")
    List<Arquivo> findBySocioIdAndTipoConteudo(@Param("socioId") Long socioId, @Param("tipoConteudo") String tipoConteudo);
    
    /**
     * Conta o número total de arquivos de um sócio.
     * 
     * @param socioId ID do sócio
     * @return Número de arquivos associados ao sócio
     */
    long countBySocioId(Long socioId);
    
    /**
     * Calcula o tamanho total dos arquivos de um sócio em bytes.
     * 
     * @param socioId ID do sócio
     * @return Soma do tamanho de todos os arquivos do sócio
     */
    @Query("SELECT COALESCE(SUM(a.tamanho), 0) FROM Arquivo a WHERE a.socio.id = :socioId")
    Long sumTamanhoBySocioId(@Param("socioId") Long socioId);
    
    /**
     * Verifica se existe um arquivo com o nome especificado para um sócio.
     * 
     * @param socioId ID do sócio
     * @param nomeArquivo Nome do arquivo no sistema
     * @return true se existe um arquivo com o nome, false caso contrário
     */
    boolean existsBySocioIdAndNomeArquivo(Long socioId, String nomeArquivo);
    
    /**
     * Busca um arquivo pelo caminho completo.
     * 
     * @param caminhoArquivo Caminho completo do arquivo no sistema de arquivos
     * @return Lista de arquivos com o caminho especificado (normalmente 0 ou 1)
     */
    List<Arquivo> findByCaminhoArquivo(String caminhoArquivo);
}
