package com.sindicato.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.dto.PastaRequest;
import com.sindicato.exception.BusinessException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Pasta;
import com.sindicato.repository.PastaRepository;

@Service
public class PastaService {
    
    private static final Logger logger = LoggerFactory.getLogger(PastaService.class);
    
    private final PastaRepository pastaRepository;
    private final AuditService auditService;
    
    public PastaService(PastaRepository pastaRepository, AuditService auditService) {
        this.pastaRepository = pastaRepository;
        this.auditService = auditService;
    }
    
    @Transactional
    public Pasta criarPasta(PastaRequest request) {
        logger.info("Criando pasta: {}", request.getNome());
        
        Pasta pasta = new Pasta();
        pasta.setNome(request.getNome());
        pasta.setDescricao(request.getDescricao());
        
        if (request.getPastaPaiId() != null) {
            Pasta pastaPai = buscarPasta(request.getPastaPaiId());
            validarNomeDuplicado(request.getNome(), pastaPai.getId());
            pasta.setPastaPai(pastaPai);
        } else {
            validarNomeDuplicadoRaiz(request.getNome());
        }
        
        Pasta savedPasta = pastaRepository.save(pasta);
        auditService.logCriacao("Pasta", savedPasta.getId(), savedPasta);
        
        logger.info("Pasta criada com sucesso: ID {}", savedPasta.getId());
        return savedPasta;
    }
    
    @Transactional(readOnly = true)
    public long contarSubpastas(Long pastaId) {
        return pastaRepository.countSubpastasByPastaId(pastaId);
    }
    
    @Transactional(readOnly = true)
    public long contarArquivos(Long pastaId) {
        return pastaRepository.countArquivosByPastaId(pastaId);
    }
    
    @Transactional(readOnly = true)
    public Pasta buscarPasta(Long id) {
        return pastaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pasta", "id", id));
    }
    
    @Transactional(readOnly = true)
    public List<Pasta> listarPastasRaiz() {
        return pastaRepository.findByPastaPaiIsNull();
    }
    
    @Transactional(readOnly = true)
    public List<Pasta> listarSubpastas(Long pastaPaiId) {
        if (!pastaRepository.existsById(pastaPaiId)) {
            throw new ResourceNotFoundException("Pasta", "id", pastaPaiId);
        }
        return pastaRepository.findByPastaPaiId(pastaPaiId);
    }
    
    @Transactional
    public Pasta atualizarPasta(Long id, PastaRequest request) {
        logger.info("Atualizando pasta ID: {}", id);
        
        Pasta pasta = buscarPasta(id);
        Pasta pastaAntes = copiarPasta(pasta);
        
        if (!pasta.getNome().equals(request.getNome())) {
            if (pasta.getPastaPai() != null) {
                validarNomeDuplicado(request.getNome(), pasta.getPastaPai().getId());
            } else {
                validarNomeDuplicadoRaiz(request.getNome());
            }
        }
        
        pasta.setNome(request.getNome());
        pasta.setDescricao(request.getDescricao());
        
        Pasta savedPasta = pastaRepository.save(pasta);
        auditService.logAtualizacao("Pasta", id, pastaAntes, savedPasta);
        
        logger.info("Pasta atualizada com sucesso: ID {}", id);
        return savedPasta;
    }
    
    @Transactional
    public void excluirPasta(Long id) {
        logger.info("Excluindo pasta ID: {}", id);
        
        Pasta pasta = buscarPasta(id);
        
        if (!pasta.getSubpastas().isEmpty()) {
            throw new BusinessException("PASTA_HAS_SUBFOLDERS", 
                "Não é possível excluir pasta com subpastas. Exclua as subpastas primeiro.");
        }
        
        if (!pasta.getArquivos().isEmpty()) {
            throw new BusinessException("PASTA_HAS_FILES", 
                "Não é possível excluir pasta com arquivos. Exclua os arquivos primeiro.");
        }
        
        Pasta pastaCopy = copiarPasta(pasta);
        pastaRepository.delete(pasta);
        auditService.logExclusao("Pasta", id, pastaCopy);
        
        logger.info("Pasta excluída com sucesso: ID {}", id);
    }
    
    private void validarNomeDuplicado(String nome, Long pastaPaiId) {
        List<Pasta> pastasExistentes = pastaRepository.findByNomeAndPastaPaiId(nome, pastaPaiId);
        if (!pastasExistentes.isEmpty()) {
            throw new BusinessException("DUPLICATE_FOLDER_NAME", 
                "Já existe uma pasta com este nome neste local");
        }
    }
    
    private void validarNomeDuplicadoRaiz(String nome) {
        List<Pasta> pastasExistentes = pastaRepository.findByNomeAndPastaPaiIsNull(nome);
        if (!pastasExistentes.isEmpty()) {
            throw new BusinessException("DUPLICATE_FOLDER_NAME", 
                "Já existe uma pasta com este nome na raiz");
        }
    }
    
    private Pasta copiarPasta(Pasta original) {
        Pasta copy = new Pasta();
        copy.setId(original.getId());
        copy.setNome(original.getNome());
        copy.setDescricao(original.getDescricao());
        return copy;
    }
}
