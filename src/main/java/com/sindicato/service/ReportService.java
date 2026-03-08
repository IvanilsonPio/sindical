package com.sindicato.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.dto.ActivityReportResponse;
import com.sindicato.model.TipoOperacao;
import com.sindicato.repository.AuditLogRepository;

@Service
public class ReportService {
    
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    
    private final AuditLogRepository auditLogRepository;
    
    public ReportService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    /**
     * Gera relatório de atividades do sistema para um período específico
     */
    @Transactional(readOnly = true)
    public ActivityReportResponse gerarRelatorioAtividade(LocalDateTime inicio, LocalDateTime fim) {
        log.info("Gerando relatório de atividade para período: {} a {}", inicio, fim);
        
        // Buscar todas as operações do período
        List<Object[]> operacoesPorUsuario = auditLogRepository.findTopUsuariosByPeriodo(inicio, fim);
        
        // Contar operações por entidade
        Map<String, Long> operacoesPorEntidade = new HashMap<>();
        operacoesPorEntidade.put("Socio", contarOperacoesPorEntidade("Socio", inicio, fim));
        operacoesPorEntidade.put("Pagamento", contarOperacoesPorEntidade("Pagamento", inicio, fim));
        operacoesPorEntidade.put("Arquivo", contarOperacoesPorEntidade("Arquivo", inicio, fim));
        operacoesPorEntidade.put("Usuario", contarOperacoesPorEntidade("Usuario", inicio, fim));
        
        // Contar operações por tipo
        Map<String, Long> operacoesPorTipo = new HashMap<>();
        operacoesPorTipo.put("CREACAO", contarOperacoesPorTipo(TipoOperacao.CREACAO, inicio, fim));
        operacoesPorTipo.put("ATUALIZACAO", contarOperacoesPorTipo(TipoOperacao.ATUALIZACAO, inicio, fim));
        operacoesPorTipo.put("EXCLUSAO", contarOperacoesPorTipo(TipoOperacao.EXCLUSAO, inicio, fim));
        
        // Contar operações por usuário
        Map<String, Long> operacoesPorUsuarioMap = new HashMap<>();
        for (Object[] row : operacoesPorUsuario) {
            String usuario = (String) row[0];
            Long total = ((Number) row[1]).longValue();
            operacoesPorUsuarioMap.put(usuario, total);
        }
        
        // Calcular totais específicos
        Long totalSociosCriados = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo(
                "Socio", TipoOperacao.CREACAO, inicio, fim);
        Long totalPagamentosRegistrados = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo(
                "Pagamento", TipoOperacao.CREACAO, inicio, fim);
        Long totalArquivosUpload = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo(
                "Arquivo", TipoOperacao.CREACAO, inicio, fim);
        
        // Calcular total geral
        Long totalOperacoes = operacoesPorTipo.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        
        ActivityReportResponse relatorio = new ActivityReportResponse();
        relatorio.setPeriodoInicio(inicio);
        relatorio.setPeriodoFim(fim);
        relatorio.setTotalOperacoes(totalOperacoes);
        relatorio.setOperacoesPorEntidade(operacoesPorEntidade);
        relatorio.setOperacoesPorTipo(operacoesPorTipo);
        relatorio.setOperacoesPorUsuario(operacoesPorUsuarioMap);
        relatorio.setTotalSociosCriados(totalSociosCriados);
        relatorio.setTotalPagamentosRegistrados(totalPagamentosRegistrados);
        relatorio.setTotalArquivosUpload(totalArquivosUpload);
        
        return relatorio;
    }
    
    private Long contarOperacoesPorEntidade(String entidade, LocalDateTime inicio, LocalDateTime fim) {
        Long create = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo(entidade, TipoOperacao.CREACAO, inicio, fim);
        Long update = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo(entidade, TipoOperacao.ATUALIZACAO, inicio, fim);
        Long delete = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo(entidade, TipoOperacao.EXCLUSAO, inicio, fim);
        return create + update + delete;
    }
    
    private Long contarOperacoesPorTipo(TipoOperacao operacao, LocalDateTime inicio, LocalDateTime fim) {
        Long socios = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo("Socio", operacao, inicio, fim);
        Long pagamentos = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo("Pagamento", operacao, inicio, fim);
        Long arquivos = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo("Arquivo", operacao, inicio, fim);
        Long usuarios = auditLogRepository.countByEntidadeAndOperacaoAndPeriodo("Usuario", operacao, inicio, fim);
        return socios + pagamentos + arquivos + usuarios;
    }
}
