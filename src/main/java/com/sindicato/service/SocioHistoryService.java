package com.sindicato.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.model.Socio;
import com.sindicato.model.SocioHistory;
import com.sindicato.model.TipoOperacao;
import com.sindicato.repository.SocioHistoryRepository;

/**
 * Service for tracking changes to Socio entities.
 * Implements change history as required by Requisito 2.3.
 */
@Service
@Transactional
public class SocioHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(SocioHistoryService.class);
    
    private final SocioHistoryRepository historyRepository;
    
    public SocioHistoryService(SocioHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }
    
    /**
     * Records the creation of a new socio.
     * 
     * @param socio The newly created socio
     * @param usuario Usuario who performed the action
     */
    public void recordCreation(Socio socio, String usuario) {
        logger.debug("Recording creation history for socio id: {}", socio.getId());
        
        SocioHistory history = new SocioHistory();
        history.setSocioId(socio.getId());
        history.setTipoOperacao(TipoOperacao.CREACAO);
        history.setDadosAnteriores(null);
        history.setDadosNovos(serializeSocio(socio));
        history.setUsuario(usuario);
        history.setDataOperacao(LocalDateTime.now());
        
        historyRepository.save(history);
        logger.info("Creation history recorded for socio id: {}", socio.getId());
    }
    
    /**
     * Records an update to an existing socio.
     * 
     * @param oldSocio The socio state before update
     * @param newSocio The socio state after update
     * @param usuario Usuario who performed the action
     */
    public void recordUpdate(Socio oldSocio, Socio newSocio, String usuario) {
        logger.debug("Recording update history for socio id: {}", newSocio.getId());
        
        SocioHistory history = new SocioHistory();
        history.setSocioId(newSocio.getId());
        history.setTipoOperacao(TipoOperacao.ATUALIZACAO);
        history.setDadosAnteriores(serializeSocio(oldSocio));
        history.setDadosNovos(serializeSocio(newSocio));
        history.setUsuario(usuario);
        history.setDataOperacao(LocalDateTime.now());
        
        historyRepository.save(history);
        logger.info("Update history recorded for socio id: {}", newSocio.getId());
    }
    
    /**
     * Records the deletion (soft delete) of a socio.
     * 
     * @param socio The socio before deletion
     * @param usuario Usuario who performed the action
     */
    public void recordDeletion(Socio socio, String usuario) {
        logger.debug("Recording deletion history for socio id: {}", socio.getId());
        
        SocioHistory history = new SocioHistory();
        history.setSocioId(socio.getId());
        history.setTipoOperacao(TipoOperacao.EXCLUSAO);
        history.setDadosAnteriores(serializeSocio(socio));
        history.setDadosNovos(null);
        history.setUsuario(usuario);
        history.setDataOperacao(LocalDateTime.now());
        
        historyRepository.save(history);
        logger.info("Deletion history recorded for socio id: {}", socio.getId());
    }
    
    /**
     * Gets all history records for a socio.
     * 
     * @param socioId Socio ID
     * @return Iterable of SocioHistory records
     */
    @Transactional(readOnly = true)
    public Iterable<SocioHistory> getHistoryBySocioId(Long socioId) {
        return historyRepository.findBySocioIdOrderByDataOperacaoDesc(socioId);
    }
    
    /**
     * Serializes socio data to JSON for storage in history.
     * 
     * @param socio The socio to serialize
     * @return JSON string representation
     */
    private String serializeSocio(Socio socio) {
        // Simple serialization - in production, use Jackson ObjectMapper
        return String.format(
            "{\"id\":%d,\"nome\":\"%s\",\"cpf\":\"%s\",\"matricula\":\"%s\",\"status\":\"%s\",\"criadoEm\":\"%s\",\"atualizadoEm\":\"%s\"}",
            socio.getId(),
            escapeJson(socio.getNome()),
            socio.getCpf(),
            escapeJson(socio.getMatricula()),
            socio.getStatus(),
            socio.getCriadoEm(),
            socio.getAtualizadoEm()
        );
    }
    
    /**
     * Escapes special characters for JSON.
     * 
     * @param value The value to escape
     * @return Escaped value
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}