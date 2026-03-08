package com.sindicato.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.dto.PagamentoRequest;
import com.sindicato.dto.PagamentoResponse;
import com.sindicato.exception.BusinessException;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Pagamento;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusPagamento;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.PagamentoRepository;
import com.sindicato.repository.SocioRepository;

/**
 * Service for managing Pagamento entities with business rules and validations.
 * Implements payment registration, cancellation, and automatic member status updates.
 * 
 * Requirements:
 * - 3.2: Automatically update member payment status when payment is registered
 * - 3.4: Prevent duplicate payment registration for same member in same month
 * - 3.5: Update member status when payment is cancelled and maintain operation record
 */
@Service
@Transactional
public class PagamentoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);
    
    private final PagamentoRepository pagamentoRepository;
    private final SocioRepository socioRepository;
    private final ReciboService reciboService;
    private final AuditService auditService;
    
    public PagamentoService(PagamentoRepository pagamentoRepository, SocioRepository socioRepository, 
                           ReciboService reciboService, AuditService auditService) {
        this.pagamentoRepository = pagamentoRepository;
        this.socioRepository = socioRepository;
        this.reciboService = reciboService;
        this.auditService = auditService;
    }
    
    /**
     * Registers a new payment with validations.
     * Validates duplicate payments and automatically updates member status.
     * 
     * @param request PagamentoRequest with payment data
     * @return PagamentoResponse of the created payment
     * @throws ResourceNotFoundException if socio not found
     * @throws DuplicateEntryException if payment already exists for the period
     * @throws BusinessException if validation fails
     */
    public PagamentoResponse registrarPagamento(PagamentoRequest request) {
        logger.info("Registering payment for socio {} - period: {}/{}", 
                request.getSocioId(), request.getMes(), request.getAno());
        
        // Find socio
        Socio socio = socioRepository.findById(request.getSocioId())
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", request.getSocioId()));
        
        // Validate duplicate payment (Requirement 3.4)
        if (pagamentoRepository.existsBySocioIdAndMesAndAno(
                request.getSocioId(), request.getMes(), request.getAno())) {
            throw new DuplicateEntryException(
                    "Pagamento", 
                    String.format("Sócio %s já possui pagamento registrado para %d/%d", 
                            socio.getNome(), request.getMes(), request.getAno()));
        }
        
        // Validate payment date is not in the future
        if (request.getDataPagamento().isAfter(LocalDate.now())) {
            throw new BusinessException("Data de pagamento não pode ser no futuro");
        }
        
        // Generate receipt number
        String numeroRecibo = gerarNumeroRecibo();
        
        // Create payment
        Pagamento pagamento = new Pagamento();
        pagamento.setSocio(socio);
        pagamento.setValor(request.getValor());
        pagamento.setMes(request.getMes());
        pagamento.setAno(request.getAno());
        pagamento.setDataPagamento(request.getDataPagamento());
        pagamento.setNumeroRecibo(numeroRecibo);
        pagamento.setObservacoes(request.getObservacoes());
        pagamento.setStatus(StatusPagamento.PAGO);
        
        Pagamento savedPagamento = pagamentoRepository.save(pagamento);
        logger.info("Payment registered successfully with id: {} and receipt: {}", 
                savedPagamento.getId(), numeroRecibo);
        
        // Automatically generate and save receipt (Requirement 4.4)
        try {
            String caminhoRecibo = reciboService.gerarESalvarRecibo(savedPagamento.getId());
            logger.info("Receipt automatically generated and saved at: {}", caminhoRecibo);
        } catch (Exception e) {
            logger.error("Failed to generate receipt for payment {}, but payment was saved", savedPagamento.getId(), e);
            // Don't fail the payment registration if receipt generation fails
        }
        
        // Automatically update member status (Requirement 3.2)
        atualizarStatusAdimplencia(socio);
        
        // Log audit
        auditService.logCriacao("Pagamento", savedPagamento.getId(), savedPagamento);
        
        return new PagamentoResponse(savedPagamento);
    }
    
    /**
     * Cancels a payment and updates member status.
     * Maintains operation record by changing status instead of deleting.
     * 
     * @param id Payment ID
     * @throws ResourceNotFoundException if payment not found
     * @throws BusinessException if payment is already cancelled
     */
    public void cancelarPagamento(Long id) {
        logger.info("Cancelling payment with id: {}", id);
        
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "id", id));
        
        // Validate payment is not already cancelled
        if (pagamento.getStatus() == StatusPagamento.CANCELADO) {
            throw new BusinessException("Pagamento já está cancelado");
        }
        
        // Change status to CANCELADO (maintains record for audit - Requirement 3.5)
        Pagamento oldPagamento = copiarPagamento(pagamento);
        pagamento.setStatus(StatusPagamento.CANCELADO);
        pagamentoRepository.save(pagamento);
        
        logger.info("Payment {} cancelled successfully", id);
        
        // Update member status after cancellation (Requirement 3.5)
        atualizarStatusAdimplencia(pagamento.getSocio());
        
        // Log audit
        auditService.logExclusao("Pagamento", id, oldPagamento);
    }
    
    private Pagamento copiarPagamento(Pagamento original) {
        Pagamento copy = new Pagamento();
        copy.setId(original.getId());
        copy.setSocio(original.getSocio());
        copy.setValor(original.getValor());
        copy.setMes(original.getMes());
        copy.setAno(original.getAno());
        copy.setDataPagamento(original.getDataPagamento());
        copy.setNumeroRecibo(original.getNumeroRecibo());
        copy.setObservacoes(original.getObservacoes());
        copy.setStatus(original.getStatus());
        return copy;
    }
    
    /**
     * Automatically updates member payment status based on current payments.
     * A member is considered ATIVO if they have paid the current or previous month.
     * Otherwise, they are marked as INATIVO.
     * 
     * This implements Requirement 3.2 and 3.5.
     * 
     * @param socio Socio to update status
     */
    private void atualizarStatusAdimplencia(Socio socio) {
        logger.debug("Updating payment status for socio: {}", socio.getId());
        
        LocalDate hoje = LocalDate.now();
        YearMonth mesAtual = YearMonth.from(hoje);
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        
        // Check if member has paid current or previous month
        boolean pagouMesAtual = pagamentoRepository.existsBySocioIdAndMesAndAno(
                socio.getId(), mesAtual.getMonthValue(), mesAtual.getYear());
        
        boolean pagouMesAnterior = pagamentoRepository.existsBySocioIdAndMesAndAno(
                socio.getId(), mesAnterior.getMonthValue(), mesAnterior.getYear());
        
        // Update status based on payment history
        StatusSocio novoStatus;
        if (pagouMesAtual || pagouMesAnterior) {
            novoStatus = StatusSocio.ATIVO;
            logger.debug("Socio {} is up to date with payments", socio.getId());
        } else {
            novoStatus = StatusSocio.INATIVO;
            logger.debug("Socio {} is not up to date with payments", socio.getId());
        }
        
        // Only update if status changed
        if (socio.getStatus() != novoStatus) {
            socio.setStatus(novoStatus);
            socioRepository.save(socio);
            logger.info("Socio {} status updated to {}", socio.getId(), novoStatus);
        }
    }
    
    /**
     * Generates a unique sequential receipt number.
     * Format: REC-YYYYMMDD-NNNN
     * 
     * @return Unique receipt number
     */
    private String gerarNumeroRecibo() {
        String dataAtual = LocalDate.now().toString().replace("-", "");
        
        // Get last receipt number
        String ultimoNumero = pagamentoRepository.findTopByOrderByNumeroReciboDesc()
                .map(Pagamento::getNumeroRecibo)
                .orElse("REC-00000000-0000");
        
        // Extract sequence number
        String[] partes = ultimoNumero.split("-");
        int sequencia = 1;
        
        if (partes.length == 3) {
            try {
                sequencia = Integer.parseInt(partes[2]) + 1;
            } catch (NumberFormatException e) {
                logger.warn("Could not parse receipt number sequence, starting from 1");
            }
        }
        
        return String.format("REC-%s-%04d", dataAtual, sequencia);
    }
    
    /**
     * Lists all payments with pagination.
     * 
     * @param pageable Pagination configuration
     * @return Page of PagamentoResponse objects
     */
    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarPagamentos(Pageable pageable) {
        logger.debug("Listing all payments");
        return pagamentoRepository.findAll(pageable)
                .map(PagamentoResponse::new);
    }
    
    /**
     * Lists payments by socio.
     * 
     * @param socioId Socio ID
     * @param pageable Pagination configuration
     * @return Page of PagamentoResponse objects
     */
    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarPagamentosPorSocio(Long socioId, Pageable pageable) {
        logger.debug("Listing payments for socio: {}", socioId);
        return pagamentoRepository.findBySocioId(socioId, pageable)
                .map(PagamentoResponse::new);
    }
    
    /**
     * Lists payments by period.
     * 
     * @param mes Month (1-12)
     * @param ano Year
     * @param pageable Pagination configuration
     * @return Page of PagamentoResponse objects
     */
    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarPagamentosPorPeriodo(Integer mes, Integer ano, Pageable pageable) {
        logger.debug("Listing payments for period: {}/{}", mes, ano);
        return pagamentoRepository.findByMesAndAno(mes, ano, pageable)
                .map(PagamentoResponse::new);
    }
    
    /**
     * Lists payments by status.
     * 
     * @param status Payment status
     * @param pageable Pagination configuration
     * @return Page of PagamentoResponse objects
     */
    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarPagamentosPorStatus(StatusPagamento status, Pageable pageable) {
        logger.debug("Listing payments with status: {}", status);
        return pagamentoRepository.findByStatus(status, pageable)
                .map(PagamentoResponse::new);
    }
    
    /**
     * Finds a payment by ID.
     * 
     * @param id Payment ID
     * @return PagamentoResponse
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorId(Long id) {
        logger.debug("Finding payment by id: {}", id);
        
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "id", id));
        
        return new PagamentoResponse(pagamento);
    }
    
    /**
     * Finds a payment by receipt number.
     * 
     * @param numeroRecibo Receipt number
     * @return PagamentoResponse
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional(readOnly = true)
    public PagamentoResponse buscarPorNumeroRecibo(String numeroRecibo) {
        logger.debug("Finding payment by receipt number: {}", numeroRecibo);
        
        Pagamento pagamento = pagamentoRepository.findByNumeroRecibo(numeroRecibo)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "numeroRecibo", numeroRecibo));
        
        return new PagamentoResponse(pagamento);
    }
    
    /**
     * Checks if a payment already exists for a member in a specific period.
     * 
     * @param socioId Socio ID
     * @param mes Month (1-12)
     * @param ano Year
     * @return true if payment exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean pagamentoJaExiste(Long socioId, Integer mes, Integer ano) {
        return pagamentoRepository.existsBySocioIdAndMesAndAno(socioId, mes, ano);
    }
    
    /**
     * Lists payments by socio and status.
     * 
     * @param socioId Socio ID
     * @param status Payment status
     * @param pageable Pagination configuration
     * @return Page of PagamentoResponse objects
     */
    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarPagamentosPorSocioEStatus(Long socioId, StatusPagamento status, Pageable pageable) {
        logger.debug("Listing payments for socio: {} with status: {}", socioId, status);
        return pagamentoRepository.findBySocioIdAndStatus(socioId, status, pageable)
                .map(PagamentoResponse::new);
    }
    
    /**
     * Lists payments by year.
     * 
     * @param ano Year
     * @param pageable Pagination configuration
     * @return Page of PagamentoResponse objects
     */
    @Transactional(readOnly = true)
    public Page<PagamentoResponse> listarPagamentosPorAno(Integer ano, Pageable pageable) {
        logger.debug("Listing payments for year: {}", ano);
        return pagamentoRepository.findByAno(ano, pageable)
                .map(PagamentoResponse::new);
    }
    
    /**
     * Counts payments by status.
     * 
     * @param status Payment status
     * @return Count of payments
     */
    @Transactional(readOnly = true)
    public long contarPorStatus(StatusPagamento status) {
        logger.debug("Counting payments with status: {}", status);
        return pagamentoRepository.countByStatus(status);
    }
    
    /**
     * Lists delinquent members (socios with no recent payments).
     * A member is considered delinquent if they haven't paid in the current or previous month.
     * 
     * @param pageable Pagination configuration
     * @return Page of maps containing socio information and last payment date
     */
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> listarInadimplentes(Pageable pageable) {
        logger.debug("Listing delinquent members");
        
        LocalDate hoje = LocalDate.now();
        
        // Get all socios with INATIVO status (delinquent members)
        Page<Socio> sociosInativos = socioRepository.findByStatus(StatusSocio.INATIVO, pageable);
        
        return sociosInativos.map(socio -> {
            Map<String, Object> inadimplente = new HashMap<>();
            inadimplente.put("socioId", socio.getId());
            inadimplente.put("nome", socio.getNome());
            inadimplente.put("cpf", socio.getCpf());
            inadimplente.put("matricula", socio.getMatricula());
            inadimplente.put("telefone", socio.getTelefone());
            inadimplente.put("status", socio.getStatus());
            
            // Find last payment date
            List<Pagamento> ultimosPagamentos = pagamentoRepository.findBySocioIdAndAno(
                    socio.getId(), hoje.getYear());
            
            if (!ultimosPagamentos.isEmpty()) {
                ultimosPagamentos.sort((p1, p2) -> {
                    int comp = Integer.compare(p2.getAno(), p1.getAno());
                    if (comp == 0) {
                        comp = Integer.compare(p2.getMes(), p1.getMes());
                    }
                    return comp;
                });
                Pagamento ultimoPagamento = ultimosPagamentos.get(0);
                inadimplente.put("ultimoPagamentoMes", ultimoPagamento.getMes());
                inadimplente.put("ultimoPagamentoAno", ultimoPagamento.getAno());
                inadimplente.put("ultimoPagamentoData", ultimoPagamento.getDataPagamento());
            } else {
                inadimplente.put("ultimoPagamentoMes", null);
                inadimplente.put("ultimoPagamentoAno", null);
                inadimplente.put("ultimoPagamentoData", null);
            }
            
            return inadimplente;
        });
    }
    
    /**
     * Generates a PDF receipt for a payment.
     * If receipt is already stored, loads from disk. Otherwise generates on-demand.
     * Implements requirement 4.5: Allow download and reprint of receipts.
     * 
     * @param pagamentoId Payment ID
     * @return PDF file as byte array
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional(readOnly = true)
    public byte[] gerarReciboPdf(Long pagamentoId) {
        logger.debug("Generating PDF receipt for payment: {}", pagamentoId);
        
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "id", pagamentoId));
        
        // If receipt is already stored, load from disk
        if (pagamento.getCaminhoRecibo() != null && !pagamento.getCaminhoRecibo().isEmpty()) {
            try {
                logger.debug("Loading stored receipt from: {}", pagamento.getCaminhoRecibo());
                return reciboService.carregarRecibo(pagamento.getCaminhoRecibo());
            } catch (Exception e) {
                logger.warn("Failed to load stored receipt, generating new one", e);
                // Fall through to generate new receipt
            }
        }
        
        // Generate receipt on-demand if not stored
        return reciboService.gerarRecibo(pagamentoId, false);
    }
    
    /**
     * Generates a second copy PDF receipt for a payment.
     * 
     * @param pagamentoId Payment ID
     * @return PDF file as byte array
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional(readOnly = true)
    public byte[] gerarReciboSegundaVia(Long pagamentoId) {
        return reciboService.gerarRecibo(pagamentoId, true);
    }
}
