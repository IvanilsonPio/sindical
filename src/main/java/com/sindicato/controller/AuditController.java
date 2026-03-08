package com.sindicato.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sindicato.dto.ActivityReportResponse;
import com.sindicato.dto.AuditLogResponse;
import com.sindicato.service.AuditService;
import com.sindicato.service.ReportService;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    
    private final AuditService auditService;
    private final ReportService reportService;
    
    public AuditController(AuditService auditService, ReportService reportService) {
        this.auditService = auditService;
        this.reportService = reportService;
    }
    
    /**
     * Busca logs de auditoria por entidade
     */
    @GetMapping("/entidade/{entidade}")
    public ResponseEntity<Page<AuditLogResponse>> buscarPorEntidade(
            @PathVariable String entidade,
            Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.buscarPorEntidade(entidade, pageable);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Busca logs de auditoria por entidade e ID específico
     */
    @GetMapping("/entidade/{entidade}/{id}")
    public ResponseEntity<Page<AuditLogResponse>> buscarPorEntidadeEId(
            @PathVariable String entidade,
            @PathVariable Long id,
            Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.buscarPorEntidadeEId(entidade, id, pageable);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Busca logs de auditoria por usuário
     */
    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<Page<AuditLogResponse>> buscarPorUsuario(
            @PathVariable String usuario,
            Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.buscarPorUsuario(usuario, pageable);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Busca logs de auditoria por período
     */
    @GetMapping("/periodo")
    public ResponseEntity<Page<AuditLogResponse>> buscarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.buscarPorPeriodo(inicio, fim, pageable);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Busca logs de auditoria por entidade e período
     */
    @GetMapping("/entidade/{entidade}/periodo")
    public ResponseEntity<Page<AuditLogResponse>> buscarPorEntidadeEPeriodo(
            @PathVariable String entidade,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.buscarPorEntidadeEPeriodo(entidade, inicio, fim, pageable);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Gera relatório de atividades do sistema
     */
    @GetMapping("/relatorio")
    public ResponseEntity<ActivityReportResponse> gerarRelatorioAtividade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        ActivityReportResponse relatorio = reportService.gerarRelatorioAtividade(inicio, fim);
        return ResponseEntity.ok(relatorio);
    }
}
