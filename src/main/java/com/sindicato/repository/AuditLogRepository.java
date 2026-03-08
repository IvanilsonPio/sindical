package com.sindicato.repository;

import com.sindicato.model.AuditLog;
import com.sindicato.model.TipoOperacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findByEntidade(String entidade, Pageable pageable);
    
    Page<AuditLog> findByEntidadeAndEntidadeId(String entidade, Long entidadeId, Pageable pageable);
    
    Page<AuditLog> findByUsuario(String usuario, Pageable pageable);
    
    Page<AuditLog> findByOperacao(TipoOperacao operacao, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.criadoEm BETWEEN :inicio AND :fim ORDER BY a.criadoEm DESC")
    Page<AuditLog> findByPeriodo(@Param("inicio") LocalDateTime inicio, 
                                  @Param("fim") LocalDateTime fim, 
                                  Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entidade = :entidade AND a.criadoEm BETWEEN :inicio AND :fim ORDER BY a.criadoEm DESC")
    Page<AuditLog> findByEntidadeAndPeriodo(@Param("entidade") String entidade,
                                            @Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim,
                                            Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.entidade = :entidade AND a.operacao = :operacao AND a.criadoEm BETWEEN :inicio AND :fim")
    Long countByEntidadeAndOperacaoAndPeriodo(@Param("entidade") String entidade,
                                               @Param("operacao") TipoOperacao operacao,
                                               @Param("inicio") LocalDateTime inicio,
                                               @Param("fim") LocalDateTime fim);
    
    @Query("SELECT a.usuario, COUNT(a) as total FROM AuditLog a WHERE a.criadoEm BETWEEN :inicio AND :fim GROUP BY a.usuario ORDER BY total DESC")
    List<Object[]> findTopUsuariosByPeriodo(@Param("inicio") LocalDateTime inicio,
                                            @Param("fim") LocalDateTime fim);
}
