package com.sindicato.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;

/**
 * Repositório para operações de persistência da entidade Socio.
 * Fornece métodos de busca customizados para consultas específicas.
 */
@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
    
    /**
     * Busca um sócio pelo CPF.
     * 
     * @param cpf CPF do sócio no formato XXX.XXX.XXX-XX
     * @return Optional contendo o sócio se encontrado
     */
    Optional<Socio> findByCpf(String cpf);
    
    /**
     * Busca um sócio pela matrícula.
     * 
     * @param matricula Número de matrícula do sócio
     * @return Optional contendo o sócio se encontrado
     */
    Optional<Socio> findByMatricula(String matricula);
    
    /**
     * Verifica se existe um sócio com o CPF informado.
     * 
     * @param cpf CPF a ser verificado
     * @return true se existe um sócio com o CPF, false caso contrário
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se existe um sócio com a matrícula informada.
     * 
     * @param matricula Matrícula a ser verificada
     * @return true se existe um sócio com a matrícula, false caso contrário
     */
    boolean existsByMatricula(String matricula);
    
    /**
     * Verifica se existe um sócio com o CPF informado, excluindo um ID específico.
     * Útil para validação durante atualização de cadastro.
     * 
     * @param cpf CPF a ser verificado
     * @param id ID do sócio a ser excluído da verificação
     * @return true se existe outro sócio com o CPF, false caso contrário
     */
    boolean existsByCpfAndIdNot(String cpf, Long id);
    
    /**
     * Verifica se existe um sócio com a matrícula informada, excluindo um ID específico.
     * Útil para validação durante atualização de cadastro.
     * 
     * @param matricula Matrícula a ser verificada
     * @param id ID do sócio a ser excluído da verificação
     * @return true se existe outro sócio com a matrícula, false caso contrário
     */
    boolean existsByMatriculaAndIdNot(String matricula, Long id);
    
    /**
     * Busca sócios por nome (busca parcial, case-insensitive).
     * 
     * @param nome Nome ou parte do nome a ser buscado
     * @param pageable Configuração de paginação
     * @return Página de sócios encontrados
     */
    Page<Socio> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    /**
     * Busca sócios por status.
     * 
     * @param status Status do sócio (ATIVO ou INATIVO)
     * @param pageable Configuração de paginação
     * @return Página de sócios com o status especificado
     */
    Page<Socio> findByStatus(StatusSocio status, Pageable pageable);
    
    /**
     * Busca sócios por múltiplos critérios usando query customizada.
     * Permite buscar por nome, CPF ou matrícula simultaneamente.
     * Otimizado com índices para melhor performance.
     * 
     * @param searchTerm Termo de busca a ser aplicado em nome, CPF ou matrícula
     * @param pageable Configuração de paginação
     * @return Página de sócios que correspondem aos critérios
     */
    @Query("SELECT s FROM Socio s WHERE " +
           "LOWER(s.nome) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "s.cpf LIKE CONCAT('%', :searchTerm, '%') OR " +
           "s.matricula LIKE CONCAT('%', :searchTerm, '%')")
    Page<Socio> searchByMultipleCriteria(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Busca sócios por nome e status.
     * 
     * @param nome Nome ou parte do nome
     * @param status Status do sócio
     * @param pageable Configuração de paginação
     * @return Página de sócios encontrados
     */
    Page<Socio> findByNomeContainingIgnoreCaseAndStatus(String nome, StatusSocio status, Pageable pageable);
    
    /**
     * Busca sócios por cidade.
     * 
     * @param cidade Nome da cidade
     * @param pageable Configuração de paginação
     * @return Página de sócios da cidade especificada
     */
    Page<Socio> findByCidadeIgnoreCase(String cidade, Pageable pageable);
    
    /**
     * Busca sócios por estado.
     * 
     * @param estado Sigla do estado (2 caracteres)
     * @param pageable Configuração de paginação
     * @return Página de sócios do estado especificado
     */
    Page<Socio> findByEstadoIgnoreCase(String estado, Pageable pageable);
    
    /**
     * Conta o número total de sócios por status.
     * 
     * @param status Status do sócio
     * @return Número de sócios com o status especificado
     */
    long countByStatus(StatusSocio status);
}
