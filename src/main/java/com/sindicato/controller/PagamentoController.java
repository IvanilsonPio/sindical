package com.sindicato.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sindicato.dto.PagamentoRequest;
import com.sindicato.dto.PagamentoResponse;
import com.sindicato.exception.BusinessException;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.StatusPagamento;
import com.sindicato.service.PagamentoService;

import jakarta.validation.Valid;

/**
 * REST controller for Pagamento management operations.
 * Provides endpoints for payment registration, cancellation, and queries with filters.
 * 
 * Requirements: 3.1, 3.3
 */
@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {
    
    private static final Logger logger = LoggerFactory.getLogger(PagamentoController.class);
    
    private final PagamentoService pagamentoService;
    
    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }
    
    /**
     * Lists all payments with optional filters and pagination.
     * Supports filtering by period (month/year), socio, and payment status.
     * 
     * Requirement 3.3: Allow filtering by period, socio, and payment status
     * 
     * @param socioId Optional socio ID filter
     * @param mes Optional month filter (1-12)
     * @param ano Optional year filter
     * @param status Optional payment status filter
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort field (default: dataPagamento)
     * @param direction Sort direction (default: DESC)
     * @return Page of PagamentoResponse objects
     */
    @GetMapping
    public ResponseEntity<Page<PagamentoResponse>> listarPagamentos(
            @RequestParam(required = false) Long socioId,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) StatusPagamento status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dataPagamento") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        logger.debug("GET /api/pagamentos - socioId: {}, mes: {}, ano: {}, status: {}, page: {}, size: {}", 
                     socioId, mes, ano, status, page, size);
        
        // Create pageable with sorting
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<PagamentoResponse> pagamentos;
        
        // Apply filters based on provided parameters
        if (socioId != null && status != null) {
            // Filter by socio and status
            pagamentos = pagamentoService.listarPagamentosPorSocioEStatus(socioId, status, pageable);
        } else if (socioId != null) {
            // Filter by socio only
            pagamentos = pagamentoService.listarPagamentosPorSocio(socioId, pageable);
        } else if (mes != null && ano != null) {
            // Filter by period (month and year)
            pagamentos = pagamentoService.listarPagamentosPorPeriodo(mes, ano, pageable);
        } else if (ano != null) {
            // Filter by year only
            pagamentos = pagamentoService.listarPagamentosPorAno(ano, pageable);
        } else if (status != null) {
            // Filter by status only
            pagamentos = pagamentoService.listarPagamentosPorStatus(status, pageable);
        } else {
            // No filters - list all
            pagamentos = pagamentoService.listarPagamentos(pageable);
        }
        
        return ResponseEntity.ok(pagamentos);
    }
    
    /**
     * Gets a specific payment by ID.
     * 
     * @param id Payment ID
     * @return PagamentoResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponse> buscarPagamento(@PathVariable Long id) {
        logger.debug("GET /api/pagamentos/{}", id);
        
        PagamentoResponse pagamento = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(pagamento);
    }
    
    /**
     * Lists all payments for a specific socio.
     * Used for receipt history display.
     * 
     * Requirement 4.5: Allow viewing receipt history by socio
     * 
     * @param socioId Socio ID
     * @return List of PagamentoResponse
     */
    @GetMapping("/socio/{socioId}")
    public ResponseEntity<Page<PagamentoResponse>> listarPagamentosPorSocio(
            @PathVariable Long socioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        logger.debug("GET /api/pagamentos/socio/{}", socioId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ano", "mes"));
        Page<PagamentoResponse> pagamentos = pagamentoService.listarPagamentosPorSocio(socioId, pageable);
        
        return ResponseEntity.ok(pagamentos);
    }
    
    /**
     * Searches for a payment by receipt number.
     * 
     * @param numeroRecibo Receipt number
     * @return PagamentoResponse
     */
    @GetMapping("/recibo/{numeroRecibo}")
    public ResponseEntity<PagamentoResponse> buscarPorRecibo(@PathVariable String numeroRecibo) {
        logger.debug("GET /api/pagamentos/recibo/{}", numeroRecibo);
        
        PagamentoResponse pagamento = pagamentoService.buscarPorNumeroRecibo(numeroRecibo);
        return ResponseEntity.ok(pagamento);
    }
    
    /**
     * Gets count of payments by status.
     * 
     * @param status Status to count
     * @return Count of payments
     */
    @GetMapping("/status/{status}/count")
    public ResponseEntity<Map<String, Long>> contarPorStatus(@PathVariable StatusPagamento status) {
        logger.debug("GET /api/pagamentos/status/{}/count", status);
        
        long count = pagamentoService.contarPorStatus(status);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets list of delinquent members (socios with no recent payments).
     * Returns socios who haven't paid in the current or previous month.
     * 
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Page of delinquent socio information
     */
    @GetMapping("/inadimplentes")
    public ResponseEntity<Page<Map<String, Object>>> listarInadimplentes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("GET /api/pagamentos/inadimplentes - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nome"));
        Page<Map<String, Object>> inadimplentes = pagamentoService.listarInadimplentes(pageable);
        
        return ResponseEntity.ok(inadimplentes);
    }

    /**
     * Registers a new payment.
     * Automatically associates payment to the correct socio and period.
     * 
     * Requirement 3.1: Associate payment to correct socio and corresponding month/year
     * 
     * @param request PagamentoRequest with payment data
     * @return Created PagamentoResponse with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<PagamentoResponse> registrarPagamento(@Valid @RequestBody PagamentoRequest request) {
        logger.info("POST /api/pagamentos - Registering payment for socio {} - period: {}/{}", 
                request.getSocioId(), request.getMes(), request.getAno());
        
        PagamentoResponse pagamento = pagamentoService.registrarPagamento(request);
        
        logger.info("Payment registered successfully with id: {} and receipt: {}", 
                pagamento.getId(), pagamento.getNumeroRecibo());
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }
    
    /**
     * Cancels a payment.
     * Changes payment status to CANCELADO and updates member status.
     * Maintains operation record for audit purposes.
     * 
     * @param id Payment ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPagamento(@PathVariable Long id) {
        logger.info("DELETE /api/pagamentos/{} - Cancelling payment", id);
        
        pagamentoService.cancelarPagamento(id);
        
        logger.info("Payment {} cancelled successfully", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Generates and downloads a payment receipt as PDF.
     * 
     * @param id Payment ID
     * @return PDF file as byte array
     */
    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> gerarRecibo(@PathVariable Long id) {
        logger.info("GET /api/pagamentos/{}/recibo - Generating receipt", id);
        
        byte[] reciboPdf = pagamentoService.gerarReciboPdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "recibo-" + id + ".pdf");
        headers.setContentLength(reciboPdf.length);
        
        logger.info("Receipt generated successfully for payment {}", id);
        return ResponseEntity.ok()
                .headers(headers)
                .body(reciboPdf);
    }
    
    /**
     * Generates and downloads a second copy of payment receipt as PDF.
     * Requirement 4.3: Allow generation of second copy of receipt
     * 
     * @param id Payment ID
     * @return PDF file as byte array with "SEGUNDA VIA" marking
     */
    @GetMapping("/{id}/recibo/segunda-via")
    public ResponseEntity<byte[]> gerarReciboSegundaVia(@PathVariable Long id) {
        logger.info("GET /api/pagamentos/{}/recibo/segunda-via - Generating second copy receipt", id);
        
        byte[] reciboPdf = pagamentoService.gerarReciboSegundaVia(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "recibo-segunda-via-" + id + ".pdf");
        headers.setContentLength(reciboPdf.length);
        
        logger.info("Second copy receipt generated successfully for payment {}", id);
        return ResponseEntity.ok()
                .headers(headers)
                .body(reciboPdf);
    }
    
    /**
     * Checks if a payment already exists for a member in a specific period.
     * 
     * @param socioId Socio ID
     * @param mes Month (1-12)
     * @param ano Year
     * @return Map with "exists" boolean
     */
    @GetMapping("/check-pagamento")
    public ResponseEntity<Map<String, Boolean>> checkPagamentoExists(
            @RequestParam Long socioId,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {
        
        logger.debug("GET /api/pagamentos/check-pagamento - socioId: {}, mes: {}, ano: {}", 
                socioId, mes, ano);
        
        boolean exists = pagamentoService.pagamentoJaExiste(socioId, mes, ano);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    // Exception Handlers
    
    /**
     * Handles validation errors from @Valid annotation.
     * 
     * @param ex MethodArgumentNotValidException
     * @return Map of field errors with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "VAL001");
        response.put("message", "Erro de validação");
        response.put("errors", errors);
        
        logger.warn("Validation error: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles ResourceNotFoundException.
     * 
     * @param ex ResourceNotFoundException
     * @return Error response with HTTP 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("BUS001", ex.getMessage());
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles DuplicateEntryException.
     * 
     * @param ex DuplicateEntryException
     * @return Error response with HTTP 409 status
     */
    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntry(DuplicateEntryException ex) {
        ErrorResponse error = new ErrorResponse("VAL003", ex.getMessage());
        
        logger.warn("Duplicate entry: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handles BusinessException.
     * 
     * @param ex BusinessException
     * @return Error response with HTTP 400 status
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse("BUS002", ex.getMessage());
        
        logger.warn("Business exception: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handles generic exceptions.
     * 
     * @param ex Exception
     * @return Error response with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse("SYS003", "Erro interno do servidor");
        
        logger.error("Internal server error", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * DTO for error responses.
     */
    public static class ErrorResponse {
        private String code;
        private String message;
        
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
