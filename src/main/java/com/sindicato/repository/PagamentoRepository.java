package com.sindicato.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sindicato.model.Pagamento;
import com.sindicato.model.StatusPagamento;

/**
 * Repositório para operações de persistência da entidade Pagamento.
 * Fornece métodos de busca customizados para consultas específicas de pagamentos.
 */
@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    
    /**
     * Busca um pagamento pelo número do recibo.
     * 
     * @param numeroRecibo Número único do recibo
     * @return Optional contendo o pagamento se encontrado
     */
    Optional<Pagamento> findByNumeroRecibo(String numeroRecibo);
    
    /**
     * Verifica se existe um pagamento para um sócio em um período específico.
     * Otimizado para usar índice composto (socio_id, ano, mes).
     * 
     * @param socioId ID do sócio
     * @param mes Mês do pagamento (1-12)
     * @param ano Ano do pagamento
     * @return true se existe um pagamento para o período, false caso contrário
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pagamento p " +
           "WHERE p.socio.id = :socioId AND p.ano = :ano AND p.mes = :mes")
    boolean existsBySocioIdAndMesAndAno(@Param("socioId") Long socioId, 
                                        @Param("mes") Integer mes, 
                                        @Param("ano") Integer ano);
    
    /**
     * Busca todos os pagamentos de um sócio específico.
     * 
     * @param socioId ID do sócio
     * @param pageable Configuração de paginação
     * @return Página de pagamentos do sócio
     */
    Page<Pagamento> findBySocioId(Long socioId, Pageable pageable);
    
    /**
     * Busca pagamentos de um sócio por status.
     * 
     * @param socioId ID do sócio
     * @param status Status do pagamento
     * @param pageable Configuração de paginação
     * @return Página de pagamentos do sócio com o status especificado
     */
    Page<Pagamento> findBySocioIdAndStatus(Long socioId, StatusPagamento status, Pageable pageable);
    
    /**
     * Busca pagamentos por período (mês e ano).
     * 
     * @param mes Mês do pagamento (1-12)
     * @param ano Ano do pagamento
     * @param pageable Configuração de paginação
     * @return Página de pagamentos do período especificado
     */
    Page<Pagamento> findByMesAndAno(Integer mes, Integer ano, Pageable pageable);
    
    /**
     * Busca pagamentos por ano.
     * 
     * @param ano Ano do pagamento
     * @param pageable Configuração de paginação
     * @return Página de pagamentos do ano especificado
     */
    Page<Pagamento> findByAno(Integer ano, Pageable pageable);
    
    /**
     * Busca pagamentos por status.
     * 
     * @param status Status do pagamento
     * @param pageable Configuração de paginação
     * @return Página de pagamentos com o status especificado
     */
    Page<Pagamento> findByStatus(StatusPagamento status, Pageable pageable);
    
    /**
     * Busca todos os pagamentos de um sócio em um ano específico.
     * 
     * @param socioId ID do sócio
     * @param ano Ano do pagamento
     * @return Lista de pagamentos do sócio no ano especificado
     */
    List<Pagamento> findBySocioIdAndAno(Long socioId, Integer ano);
    
    /**
     * Busca um pagamento específico de um sócio em um período.
     * 
     * @param socioId ID do sócio
     * @param mes Mês do pagamento (1-12)
     * @param ano Ano do pagamento
     * @return Optional contendo o pagamento se encontrado
     */
    Optional<Pagamento> findBySocioIdAndMesAndAno(Long socioId, Integer mes, Integer ano);
    
    /**
     * Conta o número total de pagamentos por status.
     * 
     * @param status Status do pagamento
     * @return Número de pagamentos com o status especificado
     */
    long countByStatus(StatusPagamento status);
    
    /**
     * Busca o último número de recibo gerado para determinar o próximo número sequencial.
     * 
     * @return Optional contendo o último pagamento ordenado por número de recibo
     */
    @Query("SELECT p FROM Pagamento p ORDER BY p.numeroRecibo DESC")
    Optional<Pagamento> findTopByOrderByNumeroReciboDesc();
}
