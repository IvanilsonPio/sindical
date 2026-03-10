package com.sindicato.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sindicato.exception.BusinessException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.ArquivoGeral;
import com.sindicato.model.Pasta;
import com.sindicato.repository.ArquivoGeralRepository;
import com.sindicato.util.ArquivoConstants;

@Service
public class ArquivoGeralService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArquivoGeralService.class);
    private static final String DIRETORIO_ARQUIVOS_GERAIS = "arquivos-gerais";
    
    private final ArquivoGeralRepository arquivoGeralRepository;
    private final PastaService pastaService;
    private final AuditService auditService;
    private final Path fileStorageLocation;
    
    public ArquivoGeralService(
            ArquivoGeralRepository arquivoGeralRepository,
            PastaService pastaService,
            AuditService auditService,
            @Value("${file.upload-dir:./uploads}") String uploadDir) {
        
        this.arquivoGeralRepository = arquivoGeralRepository;
        this.pastaService = pastaService;
        this.auditService = auditService;
        this.fileStorageLocation = Paths.get(uploadDir, DIRETORIO_ARQUIVOS_GERAIS)
            .toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Diretório de arquivos gerais criado/verificado: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            logger.error("Erro ao criar diretório de arquivos gerais: {}", this.fileStorageLocation, ex);
            throw new BusinessException(
                "FILE_STORAGE_INIT_ERROR",
                "Não foi possível criar o diretório de armazenamento de arquivos gerais"
            );
        }
    }
    
    @Transactional
    public List<ArquivoGeral> uploadArquivos(Long pastaId, MultipartFile[] files) {
        logger.info("Iniciando upload de {} arquivo(s) para pasta ID: {}", files.length, pastaId);
        
        Pasta pasta = null;
        if (pastaId != null) {
            pasta = pastaService.buscarPasta(pastaId);
        }
        
        if (files == null || files.length == 0) {
            throw new BusinessException("NO_FILES_PROVIDED", "Nenhum arquivo foi enviado");
        }
        
        List<ArquivoGeral> arquivosSalvos = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                ArquivoGeral arquivo = processarArquivo(pasta, file);
                arquivosSalvos.add(arquivo);
                logger.info("Arquivo processado: {} (ID: {})", arquivo.getNomeOriginal(), arquivo.getId());
            } catch (Exception ex) {
                logger.error("Erro ao processar arquivo: {}", file.getOriginalFilename(), ex);
                limparArquivosSalvos(arquivosSalvos);
                throw new BusinessException(
                    "FILE_PROCESSING_ERROR",
                    "Erro ao processar arquivo: " + file.getOriginalFilename() + ". " + ex.getMessage()
                );
            }
        }
        
        logger.info("Upload concluído: {} arquivo(s) salvos", arquivosSalvos.size());
        return arquivosSalvos;
    }
    
    private ArquivoGeral processarArquivo(Pasta pasta, MultipartFile file) throws IOException {
        validarArquivo(file);
        
        String nomeOriginal = StringUtils.cleanPath(file.getOriginalFilename());
        String nomeArquivo = gerarNomeUnico(nomeOriginal);
        Path caminhoDestino = this.fileStorageLocation.resolve(nomeArquivo);
        
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, caminhoDestino, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Arquivo salvo: {}", caminhoDestino);
        }
        
        ArquivoGeral arquivo = new ArquivoGeral(
            nomeOriginal,
            nomeArquivo,
            file.getContentType(),
            file.getSize(),
            caminhoDestino.toString()
        );
        
        if (pasta != null) {
            arquivo.setPasta(pasta);
        }
        
        ArquivoGeral savedArquivo = arquivoGeralRepository.save(arquivo);
        auditService.logCriacao("ArquivoGeral", savedArquivo.getId(), savedArquivo);
        
        return savedArquivo;
    }
    
    private void validarArquivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", ArquivoConstants.ERRO_ARQUIVO_VAZIO);
        }
        
        String nomeOriginal = file.getOriginalFilename();
        if (nomeOriginal == null || nomeOriginal.contains("..")) {
            throw new BusinessException("INVALID_FILE_NAME", ArquivoConstants.ERRO_NOME_ARQUIVO_INVALIDO);
        }
        
        if (!ArquivoConstants.isTamanhoValido(file.getSize())) {
            throw new BusinessException("FILE_TOO_LARGE", ArquivoConstants.ERRO_ARQUIVO_MUITO_GRANDE);
        }
        
        if (!ArquivoConstants.isTipoConteudoPermitido(file.getContentType())) {
            throw new BusinessException("INVALID_FILE_TYPE", ArquivoConstants.ERRO_TIPO_NAO_PERMITIDO);
        }
        
        if (!ArquivoConstants.isExtensaoPermitida(nomeOriginal)) {
            throw new BusinessException("INVALID_FILE_EXTENSION", ArquivoConstants.ERRO_TIPO_NAO_PERMITIDO);
        }
    }
    
    private String gerarNomeUnico(String nomeOriginal) {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String nomeSeguro = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("%d_%s_%s", timestamp, uuid, nomeSeguro);
    }
    
    @Transactional(readOnly = true)
    public List<ArquivoGeral> listarArquivos(Long pastaId) {
        if (pastaId != null) {
            return arquivoGeralRepository.findByPastaId(pastaId);
        }
        return arquivoGeralRepository.findByPastaIsNull();
    }
    
    @Transactional(readOnly = true)
    public ArquivoGeral buscarArquivo(Long id) {
        return arquivoGeralRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ArquivoGeral", "id", id));
    }
    
    @Transactional(readOnly = true)
    public Resource downloadArquivo(Long id) {
        ArquivoGeral arquivo = buscarArquivo(id);
        
        try {
            Path caminhoArquivo = Paths.get(arquivo.getCaminhoArquivo());
            Resource resource = new UrlResource(caminhoArquivo.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(
                    "FILE_NOT_FOUND_IN_STORAGE",
                    "Arquivo não encontrado: " + arquivo.getNomeOriginal()
                );
            }
        } catch (IOException ex) {
            throw new BusinessException(
                "FILE_READ_ERROR",
                "Erro ao ler o arquivo: " + arquivo.getNomeOriginal()
            );
        }
    }
    
    @Transactional
    public void excluirArquivo(Long id) {
        ArquivoGeral arquivo = buscarArquivo(id);
        ArquivoGeral arquivoCopy = copiarArquivo(arquivo);
        
        try {
            Path caminhoArquivo = Paths.get(arquivo.getCaminhoArquivo());
            Files.deleteIfExists(caminhoArquivo);
        } catch (IOException ex) {
            throw new BusinessException(
                "FILE_DELETE_ERROR",
                "Erro ao excluir arquivo físico: " + arquivo.getNomeOriginal()
            );
        }
        
        arquivoGeralRepository.delete(arquivo);
        auditService.logExclusao("ArquivoGeral", id, arquivoCopy);
        
        logger.info("Arquivo excluído: ID {}", id);
    }
    
    private ArquivoGeral copiarArquivo(ArquivoGeral original) {
        ArquivoGeral copy = new ArquivoGeral();
        copy.setId(original.getId());
        copy.setNomeOriginal(original.getNomeOriginal());
        copy.setNomeArquivo(original.getNomeArquivo());
        copy.setTipoConteudo(original.getTipoConteudo());
        copy.setTamanho(original.getTamanho());
        return copy;
    }
    
    private void limparArquivosSalvos(List<ArquivoGeral> arquivos) {
        for (ArquivoGeral arquivo : arquivos) {
            try {
                Path caminhoArquivo = Paths.get(arquivo.getCaminhoArquivo());
                Files.deleteIfExists(caminhoArquivo);
                if (arquivo.getId() != null) {
                    arquivoGeralRepository.delete(arquivo);
                }
            } catch (Exception ex) {
                logger.error("Erro ao limpar arquivo {}", arquivo.getNomeOriginal(), ex);
            }
        }
    }
}
