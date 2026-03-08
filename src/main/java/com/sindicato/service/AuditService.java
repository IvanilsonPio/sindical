package com.sindicato.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sindicato.dto.AuditLogResponse;
import com.sindicato.model.AuditLog;
import com.sindicato.model.TipoOperacao;
import com.sindicato.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Registra uma operação de auditoria de forma assíncrona
     * Usa REQUIRES_NEW para garantir que o log seja salvo mesmo se a transação principal falhar
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperacao(String entidade, Long entidadeId, TipoOperacao operacao, 
                           Object dadosAnteriores, Object dadosNovos) {
        try {
            String usuario = obterUsuarioAtual();
            String ipAddress = obterIpAddress();
            String userAgent = obterUserAgent();
            
            AuditLog auditLog = new AuditLog();
            auditLog.setEntidade(entidade);
            auditLog.setEntidadeId(entidadeId);
            auditLog.setOperacao(operacao);
            auditLog.setUsuario(usuario);
            auditLog.setDadosAnteriores(converterParaJson(dadosAnteriores));
            auditLog.setDadosNovos(converterParaJson(dadosNovos));
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            
            auditLogRepository.save(auditLog);
            log.info("Auditoria registrada: {} {} em {} (ID: {})", operacao, entidade, entidadeId, usuario);
            
        } catch (Exception e) {
            log.error("Erro ao registrar auditoria: {}", e.getMessage(), e);
            // Não propaga a exceção para não afetar a operação principal
        }
    }
    
    /**
     * Registra criação de entidade
     */
    public void logCriacao(String entidade, Long entidadeId, Object dadosNovos) {
        logOperacao(entidade, entidadeId, TipoOperacao.CREACAO, null, dadosNovos);
    }
    
    /**
     * Registra atualização de entidade
     */
    public void logAtualizacao(String entidade, Long entidadeId, Object dadosAnteriores, Object dadosNovos) {
        logOperacao(entidade, entidadeId, TipoOperacao.ATUALIZACAO, dadosAnteriores, dadosNovos);
    }
    
    /**
     * Registra exclusão de entidade
     */
    public void logExclusao(String entidade, Long entidadeId, Object dadosAnteriores) {
        logOperacao(entidade, entidadeId, TipoOperacao.EXCLUSAO, dadosAnteriores, null);
    }
    
    /**
     * Busca logs de auditoria por entidade
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> buscarPorEntidade(String entidade, Pageable pageable) {
        return auditLogRepository.findByEntidade(entidade, pageable)
                .map(this::converterParaResponse);
    }
    
    /**
     * Busca logs de auditoria por entidade e ID
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> buscarPorEntidadeEId(String entidade, Long entidadeId, Pageable pageable) {
        return auditLogRepository.findByEntidadeAndEntidadeId(entidade, entidadeId, pageable)
                .map(this::converterParaResponse);
    }
    
    /**
     * Busca logs de auditoria por usuário
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> buscarPorUsuario(String usuario, Pageable pageable) {
        return auditLogRepository.findByUsuario(usuario, pageable)
                .map(this::converterParaResponse);
    }
    
    /**
     * Busca logs de auditoria por período
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        return auditLogRepository.findByPeriodo(inicio, fim, pageable)
                .map(this::converterParaResponse);
    }
    
    /**
     * Busca logs de auditoria por entidade e período
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> buscarPorEntidadeEPeriodo(String entidade, LocalDateTime inicio, 
                                                            LocalDateTime fim, Pageable pageable) {
        return auditLogRepository.findByEntidadeAndPeriodo(entidade, inicio, fim, pageable)
                .map(this::converterParaResponse);
    }
    
    private String obterUsuarioAtual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.warn("Erro ao obter usuário atual: {}", e.getMessage());
        }
        return "SYSTEM";
    }
    
    private String obterIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Erro ao obter IP address: {}", e.getMessage());
        }
        return null;
    }
    
    private String obterUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Erro ao obter User-Agent: {}", e.getMessage());
        }
        return null;
    }
    
    private String converterParaJson(Object objeto) {
        if (objeto == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter objeto para JSON: {}", e.getMessage());
            return objeto.toString();
        }
    }
    
    private AuditLogResponse converterParaResponse(AuditLog auditLog) {
        return new AuditLogResponse(auditLog);
    }
}
