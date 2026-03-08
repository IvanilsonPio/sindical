package com.sindicato.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sindicato.model.SocioHistory;

/**
 * Repository for SocioHistory entity operations.
 */
@Repository
public interface SocioHistoryRepository extends JpaRepository<SocioHistory, Long> {
    
    /**
     * Finds all history records for a socio, ordered by operation date descending.
     * 
     * @param socioId Socio ID
     * @return List of SocioHistory records
     */
    List<SocioHistory> findBySocioIdOrderByDataOperacaoDesc(Long socioId);
}