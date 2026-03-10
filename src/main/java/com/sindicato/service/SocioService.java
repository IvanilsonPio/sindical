package com.sindicato.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.dto.CampoAlterado;
import com.sindicato.dto.HistoricoAlteracaoResponse;
import com.sindicato.dto.SocioDetalhadoResponse;
import com.sindicato.dto.SocioRequest;
import com.sindicato.dto.SocioResponse;
import com.sindicato.dto.SocioUpdateRequest;
import com.sindicato.exception.DuplicateEntryException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Socio;
import com.sindicato.model.SocioHistory;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.SocioRepository;

/**
 * Service for managing Socio entities with business rules and validations.
 * Implements CRUD operations, search functionality, and change history tracking.
 * Uses caching to improve performance of read operations.
 */
@Service
@Transactional
public class SocioService {
    
    private static final Logger logger = LoggerFactory.getLogger(SocioService.class);
    
    private final SocioRepository socioRepository;
    private final SocioHistoryService historyService;
    private final AuditService auditService;
    
    public SocioService(SocioRepository socioRepository, SocioHistoryService historyService, AuditService auditService) {
        this.socioRepository = socioRepository;
        this.historyService = historyService;
        this.auditService = auditService;
    }
    
    /**
     * Lists all socios with pagination and optional filters.
     * 
     * @param nome Optional name filter (partial match, case-insensitive)
     * @param status Optional status filter
     * @param pageable Pagination configuration
     * @return Page of SocioResponse objects
     */
    @Transactional(readOnly = true)
    public Page<SocioResponse> listarSocios(String nome, StatusSocio status, Pageable pageable) {
        logger.debug("Listing socios with filters - nome: {}, status: {}", nome, status);
        
        Page<Socio> socios;
        
        if (nome != null && !nome.isBlank() && status != null) {
            socios = socioRepository.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable);
        } else if (nome != null && !nome.isBlank()) {
            socios = socioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (status != null) {
            socios = socioRepository.findByStatus(status, pageable);
        } else {
            socios = socioRepository.findAll(pageable);
        }
        
        return socios.map(SocioResponse::new);
    }
    
    /**
     * Searches socios by multiple criteria (nome, CPF, or matrícula).
     * 
     * @param searchTerm Termo de busca
     * @param pageable Pagination configuration
     * @return Page of SocioResponse objects matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<SocioResponse> buscarPorCriterios(String searchTerm, Pageable pageable) {
        logger.debug("Searching socios with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.isBlank()) {
            return socioRepository.findAll(pageable).map(SocioResponse::new);
        }
        
        return socioRepository.searchByMultipleCriteria(searchTerm.trim(), pageable)
                .map(SocioResponse::new);
    }
    
    /**
     * Finds a socio by ID.
     * Cached to improve performance of repeated lookups.
     * 
     * @param id Socio ID
     * @return SocioResponse
     * @throws ResourceNotFoundException if socio not found
     */
    @Cacheable(value = "socio", key = "#id")
    @Transactional(readOnly = true)
    public SocioResponse buscarPorId(Long id) {
        logger.debug("Finding socio by id: {}", id);
        
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", id));
        
        return new SocioResponse(socio);
    }

    /**
     * Gets detailed information about a socio including all relationships.
     * Uses optimized query with JOIN FETCH to load pagamentos and arquivos.
     * Pagamentos are sorted by date descending (most recent first).
     * Arquivos are sorted by upload date descending (most recent first).
     *
     * @param id Socio ID
     * @return SocioDetalhadoResponse with all socio data and relationships
     * @throws ResourceNotFoundException if socio not found
     */
    /**
     * Gets detailed information about a socio including all relationships.
     * Uses optimized query with JOIN FETCH to load pagamentos and arquivos.
     * Pagamentos are sorted by date descending (most recent first).
     * Arquivos are sorted by upload date descending (most recent first).
     * Historico is loaded and sorted by date/time descending (most recent first).
     *
     * Requirements: 1.1, 1.6, 1.7, 5.5, 5.6
     *
     * @param id Socio ID
     * @return SocioDetalhadoResponse with all socio data and relationships
     * @throws ResourceNotFoundException if socio not found
     */
    @Transactional(readOnly = true)
    public SocioDetalhadoResponse getSocioDetalhado(Long id) {
        logger.debug("Finding detailed socio information for id: {}", id);

        // Fetch socio with pagamentos first
        Socio socio = socioRepository.findByIdWithPagamentos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", id));

        // Then fetch arquivos separately to avoid MultipleBagFetchException
        socioRepository.findByIdWithArquivos(id);

        // SocioDetalhadoResponse constructor handles sorting of pagamentos and arquivos
        SocioDetalhadoResponse response = new SocioDetalhadoResponse(socio);

        // Load and add history (already sorted by date descending in getHistoricoAlteracoes)
        List<HistoricoAlteracaoResponse> historico = getHistoricoAlteracoes(id);
        response.setHistorico(historico);

        return response;
    }

    
    /**
     * Finds a socio by CPF.
     * 
     * @param cpf CPF number
     * @return SocioResponse
     * @throws ResourceNotFoundException if socio not found
     */
    @Transactional(readOnly = true)
    public SocioResponse buscarPorCpf(String cpf) {
        logger.debug("Finding socio by cpf: {}", cpf);
        
        Socio socio = socioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "cpf", cpf));
        
        return new SocioResponse(socio);
    }
    
    /**
     * Finds a socio by matrícula.
     * 
     * @param matricula Matrícula number
     * @return SocioResponse
     * @throws ResourceNotFoundException if socio not found
     */
    @Transactional(readOnly = true)
    public SocioResponse buscarPorMatricula(String matricula) {
        logger.debug("Finding socio by matricula: {}", matricula);
        
        Socio socio = socioRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "matricula", matricula));
        
        return new SocioResponse(socio);
    }
    
    /**
     * Creates a new socio with validations.
     * Evicts socios cache to ensure fresh data on next query.
     * 
     * @param request SocioRequest with socio data
     * @return SocioResponse of the created socio
     * @throws DuplicateEntryException if CPF or matrícula already exists
     */
    @CacheEvict(value = "socios", allEntries = true)
    public SocioResponse criarSocio(SocioRequest request) {
        logger.info("Creating new socio with cpf: {}", request.getCpf());
        
        // Validate unique CPF
        if (socioRepository.existsByCpf(request.getCpf())) {
            throw new DuplicateEntryException("CPF", request.getCpf());
        }
        
        // Validate unique matrícula
        if (socioRepository.existsByMatricula(request.getMatricula())) {
            throw new DuplicateEntryException("Matrícula", request.getMatricula());
        }
        
        Socio socio = mapToEntity(request);
        socio.setStatus(StatusSocio.ATIVO);
        
        Socio savedSocio = socioRepository.save(socio);
        logger.info("Socio created successfully with id: {}", savedSocio.getId());
        
        // Record creation in history
        historyService.recordCreation(savedSocio, "Sistema");
        
        // Log audit
        auditService.logCriacao("Socio", savedSocio.getId(), savedSocio);
        
        return new SocioResponse(savedSocio);
    }
    
    /**
     * Updates an existing socio with validations.
     * Evicts both individual socio cache and socios list cache.
     * 
     * @param id Socio ID
     * @param request SocioRequest with updated data
     * @return SocioResponse of the updated socio
     * @throws ResourceNotFoundException if socio not found
     * @throws DuplicateEntryException if CPF or matrícula already exists for another socio
     */
    @Caching(evict = {
        @CacheEvict(value = "socio", key = "#id"),
        @CacheEvict(value = "socios", allEntries = true)
    })
    public SocioResponse atualizarSocio(Long id, SocioRequest request) {
        logger.info("Updating socio with id: {}", id);
        
        Socio existingSocio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", id));
        
        // Store old values for history tracking
        Socio oldSocio = copySocio(existingSocio);
        
        // Validate unique CPF (excluding current socio)
        if (!existingSocio.getCpf().equals(request.getCpf()) && 
            socioRepository.existsByCpfAndIdNot(request.getCpf(), id)) {
            throw new DuplicateEntryException("CPF", request.getCpf());
        }
        
        // Validate unique matrícula (excluding current socio)
        if (!existingSocio.getMatricula().equals(request.getMatricula()) && 
            socioRepository.existsByMatriculaAndIdNot(request.getMatricula(), id)) {
            throw new DuplicateEntryException("Matrícula", request.getMatricula());
        }
        
        // Update fields
        updateEntityFromRequest(existingSocio, request);
        
        Socio updatedSocio = socioRepository.save(existingSocio);
        logger.info("Socio updated successfully with id: {}", updatedSocio.getId());
        
        // Record update in history
        historyService.recordUpdate(oldSocio, updatedSocio, "Sistema");
        
        // Log audit
        auditService.logAtualizacao("Socio", updatedSocio.getId(), oldSocio, updatedSocio);
        
        return new SocioResponse(updatedSocio);
    }
    /**
     * Updates an existing socio with validations using SocioUpdateRequest.
     * Records the username of the user who made the change for audit purposes.
     * Evicts both individual socio cache and socios list cache.
     *
     * @param id Socio ID
     * @param request SocioUpdateRequest with updated data
     * @param username Username of the authenticated user making the change
     * @return SocioResponse of the updated socio
     * @throws ResourceNotFoundException if socio not found
     * @throws DuplicateEntryException if CPF or matrícula already exists for another socio
     */
    @Caching(evict = {
        @CacheEvict(value = "socio", key = "#id"),
        @CacheEvict(value = "socios", allEntries = true)
    })
    public SocioResponse updateSocio(Long id, SocioUpdateRequest request, String username) {
        logger.info("Updating socio with id: {} by user: {}", id, username);

        Socio existingSocio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", id));

        // Store old values for history tracking
        Socio oldSocio = copySocio(existingSocio);

        // Validate unique CPF (excluding current socio)
        if (!existingSocio.getCpf().equals(request.getCpf()) &&
            socioRepository.existsByCpfAndIdNot(request.getCpf(), id)) {
            throw new DuplicateEntryException("CPF", request.getCpf());
        }

        // Validate unique matrícula (excluding current socio)
        if (!existingSocio.getMatricula().equals(request.getMatricula()) &&
            socioRepository.existsByMatriculaAndIdNot(request.getMatricula(), id)) {
            throw new DuplicateEntryException("Matrícula", request.getMatricula());
        }

        // Apply changes from SocioUpdateRequest
        updateEntityFromUpdateRequest(existingSocio, request);

        Socio updatedSocio = socioRepository.save(existingSocio);
        logger.info("Socio updated successfully with id: {} by user: {}", updatedSocio.getId(), username);

        // Record update in history with actual username
        historyService.recordUpdate(oldSocio, updatedSocio, username);

        // Log audit
        auditService.logAtualizacao("Socio", updatedSocio.getId(), oldSocio, updatedSocio);

        return new SocioResponse(updatedSocio);
    }

    
    /**
     * Soft deletes a socio by changing status to INATIVO.
     * Preserves payment records for audit purposes.
     * Evicts both individual socio cache and socios list cache.
     * 
     * @param id Socio ID
     * @throws ResourceNotFoundException if socio not found
     */
    @Caching(evict = {
        @CacheEvict(value = "socio", key = "#id"),
        @CacheEvict(value = "socios", allEntries = true)
    })
    public void excluirSocio(Long id) {
        logger.info("Soft deleting socio with id: {}", id);
        
        Socio socio = socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", id));
        
        // Soft delete - change status to INATIVO
        // Payment records are preserved for audit (requirement 2.5)
        Socio oldSocio = copySocio(socio);
        socio.setStatus(StatusSocio.INATIVO);
        socioRepository.save(socio);
        
        logger.info("Socio {} status changed to INATIVO, payments preserved for audit", id);
        
        // Record deletion in history
        historyService.recordDeletion(socio, "Sistema");
        
        // Log audit
        auditService.logExclusao("Socio", id, oldSocio);
    }
    
    /**
     * Checks if a CPF already exists in the system.
     * 
     * @param cpf CPF to check
     * @param excludeId Socio ID to exclude from check (for updates)
     * @return true if CPF exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean cpfJaExiste(String cpf, Long excludeId) {
        if (excludeId != null) {
            return socioRepository.existsByCpfAndIdNot(cpf, excludeId);
        }
        return socioRepository.existsByCpf(cpf);
    }
    
    /**
     * Checks if a matrícula already exists in the system.
     * 
     * @param matricula Matrícula to check
     * @param excludeId Socio ID to exclude from check (for updates)
     * @return true if matrícula exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean matriculaJaExiste(String matricula, Long excludeId) {
        if (excludeId != null) {
            return socioRepository.existsByMatriculaAndIdNot(matricula, excludeId);
        }
        return socioRepository.existsByMatricula(matricula);
    }
    
    /**
     * Gets socios by status.
     * 
     * @param status Status to filter by
     * @param pageable Pagination configuration
     * @return Page of SocioResponse objects
     */
    @Transactional(readOnly = true)
    public Page<SocioResponse> buscarPorStatus(StatusSocio status, Pageable pageable) {
        return socioRepository.findByStatus(status, pageable)
                .map(SocioResponse::new);
    }
    
    /**
     * Gets count of socios by status.
     * 
     * @param status Status to count
     * @return Number of socios with the specified status
     */
    @Transactional(readOnly = true)
    public long contarPorStatus(StatusSocio status) {
        return socioRepository.countByStatus(status);
    }
    
    // Private helper methods
    
    private Socio mapToEntity(SocioRequest request) {
        Socio socio = new Socio();
        socio.setNome(request.getNome());
        socio.setCpf(request.getCpf());
        socio.setMatricula(request.getMatricula());
        socio.setRg(request.getRg());
        socio.setDataNascimento(request.getDataNascimento());
        socio.setTelefone(request.getTelefone());
        socio.setEmail(request.getEmail());
        socio.setEndereco(request.getEndereco());
        socio.setCidade(request.getCidade());
        socio.setEstado(request.getEstado());
        socio.setCep(request.getCep());
        socio.setProfissao(request.getProfissao());
        return socio;
    }
    
    private void updateEntityFromRequest(Socio socio, SocioRequest request) {
        socio.setNome(request.getNome());
        socio.setCpf(request.getCpf());
        socio.setMatricula(request.getMatricula());
        socio.setRg(request.getRg());
        socio.setDataNascimento(request.getDataNascimento());
        socio.setTelefone(request.getTelefone());
        socio.setEmail(request.getEmail());
        socio.setEndereco(request.getEndereco());
        socio.setCidade(request.getCidade());
        socio.setEstado(request.getEstado());
        socio.setCep(request.getCep());
        socio.setProfissao(request.getProfissao());
    }
    
    private Socio copySocio(Socio original) {
        Socio copy = new Socio();
        copy.setId(original.getId());
        copy.setNome(original.getNome());
        copy.setCpf(original.getCpf());
        copy.setMatricula(original.getMatricula());
        copy.setRg(original.getRg());
        copy.setDataNascimento(original.getDataNascimento());
        copy.setTelefone(original.getTelefone());
        copy.setEmail(original.getEmail());
        copy.setEndereco(original.getEndereco());
        copy.setCidade(original.getCidade());
        copy.setEstado(original.getEstado());
        copy.setCep(original.getCep());
        copy.setProfissao(original.getProfissao());
        copy.setStatus(original.getStatus());
        copy.setCriadoEm(original.getCriadoEm());
        copy.setAtualizadoEm(original.getAtualizadoEm());
        return copy;
    }
    
    private void updateEntityFromUpdateRequest(Socio socio, SocioUpdateRequest request) {
        socio.setNome(request.getNome());
        socio.setCpf(request.getCpf());
        socio.setMatricula(request.getMatricula());
        socio.setRg(request.getRg());
        socio.setDataNascimento(request.getDataNascimento());
        socio.setTelefone(request.getTelefone());
        socio.setEmail(request.getEmail());
        socio.setEndereco(request.getEndereco());
        socio.setCidade(request.getCidade());
        socio.setEstado(request.getEstado());
        socio.setCep(request.getCep());
        socio.setProfissao(request.getProfissao());
        socio.setStatus(request.getStatus());
    }


    /**
     * Gets the history of changes for a specific socio.
     * Returns all alterations ordered by date/time descending (most recent first).
     *
     * Requirements: 5.5, 5.6
     *
     * @param id Socio ID
     * @return List of HistoricoAlteracaoResponse with all changes
     */
    @Transactional(readOnly = true)
    public List<HistoricoAlteracaoResponse> getHistoricoAlteracoes(Long id) {
        logger.debug("Getting history for socio id: {}", id);

        // Verify socio exists
        if (!socioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Socio", "id", id);
        }

        Iterable<SocioHistory> history = historyService.getHistoryBySocioId(id);

        List<HistoricoAlteracaoResponse> responses = new ArrayList<>();

        for (SocioHistory record : history) {
            HistoricoAlteracaoResponse response = new HistoricoAlteracaoResponse();
            response.setId(record.getId());
            response.setSocioId(record.getSocioId());
            response.setUsuario(record.getUsuario());
            response.setDataHora(record.getDataOperacao());
            response.setOperacao(record.getTipoOperacao().name());

            // Parse and compare JSON data to extract changed fields
            Map<String, CampoAlterado> camposAlterados =
                extractChangedFields(record.getDadosAnteriores(), record.getDadosNovos());
            response.setCamposAlterados(camposAlterados);

            responses.add(response);
        }

        logger.debug("Found {} history records for socio id: {}", responses.size(), id);
        return responses;
    }

    /**
     * Extracts changed fields by comparing old and new JSON data.
     *
     * @param dadosAnteriores JSON string with previous data
     * @param dadosNovos JSON string with new data
     * @return Map of field names to CampoAlterado objects
     */
    private Map<String, CampoAlterado> extractChangedFields(
            String dadosAnteriores, String dadosNovos) {

        Map<String, CampoAlterado> changes = new HashMap<>();

        // Simple JSON parsing for the fields we track
        // In production, use Jackson ObjectMapper for robust parsing

        if (dadosAnteriores == null && dadosNovos != null) {
            // Creation - all fields are new
            changes.put("status", new CampoAlterado("status", null, extractJsonValue(dadosNovos, "status")));
            return changes;
        }

        if (dadosAnteriores != null && dadosNovos == null) {
            // Deletion - all fields removed
            changes.put("status", new CampoAlterado("status", extractJsonValue(dadosAnteriores, "status"), null));
            return changes;
        }

        if (dadosAnteriores != null && dadosNovos != null) {
            // Update - compare fields
            String[] fields = {"nome", "cpf", "matricula", "status"};

            for (String field : fields) {
                String oldValue = extractJsonValue(dadosAnteriores, field);
                String newValue = extractJsonValue(dadosNovos, field);

                if (!Objects.equals(oldValue, newValue)) {
                    changes.put(field, new CampoAlterado(field, oldValue, newValue));
                }
            }
        }

        return changes;
    }

    /**
     * Extracts a value from a simple JSON string.
     * This is a basic implementation - in production use Jackson ObjectMapper.
     *
     * @param json JSON string
     * @param key Key to extract
     * @return Value as string, or null if not found
     */
    private String extractJsonValue(String json, String key) {
        if (json == null) return null;

        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;

        startIndex += searchKey.length();

        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }

        if (startIndex >= json.length()) return null;

        // Check if value is a string (starts with ")
        if (json.charAt(startIndex) == '"') {
            startIndex++; // Skip opening quote
            int endIndex = json.indexOf('"', startIndex);
            if (endIndex == -1) return null;
            return json.substring(startIndex, endIndex);
        } else {
            // Value is not a string (number, boolean, null)
            int endIndex = startIndex;
            while (endIndex < json.length() &&
                   json.charAt(endIndex) != ',' &&
                   json.charAt(endIndex) != '}') {
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }

}