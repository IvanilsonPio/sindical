package com.sindicato.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sindicato.dto.HistoricoAlteracaoResponse;
import com.sindicato.dto.SocioDetalhadoResponse;
import com.sindicato.dto.SocioRequest;
import com.sindicato.dto.SocioResponse;
import com.sindicato.dto.SocioUpdateRequest;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.StatusSocio;
import com.sindicato.service.SocioService;

import jakarta.validation.Valid;

/**
 * REST controller for Socio management operations.
 * Provides endpoints for CRUD operations with pagination and filtering.
 * 
 * Requirements: 2.1, 2.2
 */
@RestController
@RequestMapping("/api/socios")
public class SocioController {
    
    private static final Logger logger = LoggerFactory.getLogger(SocioController.class);
    
    private final SocioService socioService;
    
    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }
    
    /**
     * Lists all socios with optional filters and pagination.
     * 
     * @param nome Optional name filter (partial match, case-insensitive)
     * @param status Optional status filter
     * @param search Optional search term (searches by nome, CPF, or matrícula)
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort field (default: nome)
     * @param direction Sort direction (default: ASC)
     * @return Page of SocioResponse objects
     */
    @GetMapping
    public ResponseEntity<Page<SocioResponse>> listarSocios(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) StatusSocio status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        logger.debug("GET /api/socios - nome: {}, status: {}, search: {}, page: {}, size: {}", 
                     nome, status, search, page, size);
        
        // Create pageable with sorting
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<SocioResponse> socios;
        
        // If search parameter is provided, use multi-criteria search
        if (search != null && !search.isBlank()) {
            socios = socioService.buscarPorCriterios(search, pageable);
        } else {
            // Otherwise use standard filters
            socios = socioService.listarSocios(nome, status, pageable);
        }
        
        return ResponseEntity.ok(socios);
    }

    /**
     * Gets a specific socio by ID.
     * 
     * @param id Socio ID
     * @return SocioResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<SocioResponse> buscarSocio(@PathVariable Long id) {
        logger.debug("GET /api/socios/{}", id);
        
        SocioResponse socio = socioService.buscarPorId(id);
        return ResponseEntity.ok(socio);
    }

    /**
     * Gets detailed information about a specific socio including all relationships.
     * Returns complete socio data with pagamentos and arquivos.
     *
     * Requirements: 1.1, 6.1
     *
     * @param id Socio ID
     * @return SocioDetalhadoResponse with all socio data
     * @throws ResourceNotFoundException if socio not found (returns 404)
     */
    @GetMapping("/{id}/detalhes")
    public ResponseEntity<SocioDetalhadoResponse> getSocioDetalhado(@PathVariable Long id) {
        logger.debug("GET /api/socios/{}/detalhes", id);

        SocioDetalhadoResponse socio = socioService.getSocioDetalhado(id);
        return ResponseEntity.ok(socio);
    }

    /**
     * Gets the history of changes for a specific socio.
     * Returns all alterations ordered by date/time descending (most recent first).
     *
     * Requirements: 5.5, 5.6
     *
     * @param id Socio ID
     * @return List of HistoricoAlteracaoResponse with all changes
     * @throws ResourceNotFoundException if socio not found (returns 404)
     */
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoAlteracaoResponse>> getHistoricoAlteracoes(@PathVariable Long id) {
        logger.debug("GET /api/socios/{}/historico", id);

        List<HistoricoAlteracaoResponse> historico = socioService.getHistoricoAlteracoes(id);
        return ResponseEntity.ok(historico);
    }


    
    /**
     * Searches for a socio by CPF.
     * 
     * @param cpf CPF number
     * @return SocioResponse
     */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<SocioResponse> buscarPorCpf(@PathVariable String cpf) {
        logger.debug("GET /api/socios/cpf/{}", cpf);
        
        SocioResponse socio = socioService.buscarPorCpf(cpf);
        return ResponseEntity.ok(socio);
    }
    
    /**
     * Searches for a socio by matrícula.
     * 
     * @param matricula Matrícula number
     * @return SocioResponse
     */
    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<SocioResponse> buscarPorMatricula(@PathVariable String matricula) {
        logger.debug("GET /api/socios/matricula/{}", matricula);
        
        SocioResponse socio = socioService.buscarPorMatricula(matricula);
        return ResponseEntity.ok(socio);
    }
    
    /**
     * Gets socios by status with pagination.
     * 
     * @param status Status to filter by
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @return Page of SocioResponse objects
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<SocioResponse>> buscarPorStatus(
            @PathVariable StatusSocio status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.debug("GET /api/socios/status/{} - page: {}, size: {}", status, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nome"));
        Page<SocioResponse> socios = socioService.buscarPorStatus(status, pageable);
        
        return ResponseEntity.ok(socios);
    }
    
    /**
     * Gets count of socios by status.
     * 
     * @param status Status to count
     * @return Count of socios
     */
    @GetMapping("/status/{status}/count")
    public ResponseEntity<Map<String, Long>> contarPorStatus(@PathVariable StatusSocio status) {
        logger.debug("GET /api/socios/status/{}/count", status);
        
        long count = socioService.contarPorStatus(status);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new socio.
     * 
     * @param request SocioRequest with socio data
     * @return Created SocioResponse with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<SocioResponse> criarSocio(@Valid @RequestBody SocioRequest request) {
        logger.info("POST /api/socios - Creating socio with cpf: {}", request.getCpf());
        
        SocioResponse socio = socioService.criarSocio(request);
        
        logger.info("Socio created successfully with id: {}", socio.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(socio);
    }
    
    /**
     * Updates an existing socio.
     * 
     * @param id Socio ID
     * @param request SocioRequest with updated data
     * @return Updated SocioResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<SocioResponse> atualizarSocio(
            @PathVariable Long id,
            @Valid @RequestBody SocioRequest request) {
        
        logger.info("PUT /api/socios/{} - Updating socio", id);
        
        SocioResponse socio = socioService.atualizarSocio(id, request);
        
        logger.info("Socio {} updated successfully", id);
        return ResponseEntity.ok(socio);
    }
    
    /**
     * Updates an existing socio with full validation and audit tracking.
     * Uses SocioUpdateRequest with comprehensive Bean Validation.
     * Captures authenticated user for audit trail.
     * 
     * Requirements: 2.1, 2.8, 6.2, 6.5
     * 
     * @param id Socio ID
     * @param request SocioUpdateRequest with updated data
     * @param userDetails Authenticated user details
     * @return Updated SocioResponse
     * @throws ResourceNotFoundException if socio not found (returns 404)
     * @throws DuplicateEntryException if CPF or matrícula conflicts (returns 409)
     */
    @PutMapping("/{id}/update")
    public ResponseEntity<SocioResponse> updateSocio(
            @PathVariable Long id,
            @Valid @RequestBody SocioUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails != null ? userDetails.getUsername() : "Sistema";
        logger.info("PUT /api/socios/{}/update - Updating socio by user: {}", id, username);
        
        SocioResponse socio = socioService.updateSocio(id, request, username);
        
        logger.info("Socio {} updated successfully by user: {}", id, username);
        return ResponseEntity.ok(socio);
    }
    
    /**
     * Soft deletes a socio (changes status to INATIVO).
     * Preserves payment records for audit purposes.
     * 
     * @param id Socio ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirSocio(@PathVariable Long id) {
        logger.info("DELETE /api/socios/{} - Soft deleting socio", id);
        
        socioService.excluirSocio(id);
        
        logger.info("Socio {} soft deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Checks if a CPF already exists in the system.
     * 
     * @param cpf CPF to check
     * @param excludeId Optional socio ID to exclude from check (for updates)
     * @return Map with "exists" boolean
     */
    @GetMapping("/check-cpf")
    public ResponseEntity<Map<String, Boolean>> checkCpfExists(
            @RequestParam String cpf,
            @RequestParam(required = false) Long excludeId) {
        
        logger.debug("GET /api/socios/check-cpf - cpf: {}, excludeId: {}", cpf, excludeId);
        
        boolean exists = socioService.cpfJaExiste(cpf, excludeId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Checks if a matrícula already exists in the system.
     * 
     * @param matricula Matrícula to check
     * @param excludeId Optional socio ID to exclude from check (for updates)
     * @return Map with "exists" boolean
     */
    @GetMapping("/check-matricula")
    public ResponseEntity<Map<String, Boolean>> checkMatriculaExists(
            @RequestParam String matricula,
            @RequestParam(required = false) Long excludeId) {
        
        logger.debug("GET /api/socios/check-matricula - matricula: {}, excludeId: {}", matricula, excludeId);
        
        boolean exists = socioService.matriculaJaExiste(matricula, excludeId);
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
