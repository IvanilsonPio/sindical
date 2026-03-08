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
import com.sindicato.model.Arquivo;
import com.sindicato.model.Socio;
import com.sindicato.repository.ArquivoRepository;
import com.sindicato.repository.SocioRepository;
import com.sindicato.util.ArquivoConstants;

/**
 * Serviço para gerenciamento de arquivos associados a sócios.
 * Implementa upload múltiplo, validação de formato e tamanho, e armazenamento organizado.
 */
@Service
public class ArquivoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ArquivoService.class);
    
    private final ArquivoRepository arquivoRepository;
    private final SocioRepository socioRepository;
    private final AuditService auditService;
    private final Path fileStorageLocation;
    
    /**
     * Construtor que inicializa o serviço e cria o diretório de armazenamento.
     * 
     * @param arquivoRepository Repositório de arquivos
     * @param socioRepository Repositório de sócios
     * @param auditService Serviço de auditoria
     * @param uploadDir Diretório base para upload (configurado em application.yml)
     */
    public ArquivoService(
            ArquivoRepository arquivoRepository,
            SocioRepository socioRepository,
            AuditService auditService,
            @Value("${file.upload-dir:./uploads}") String uploadDir) {
        
        this.arquivoRepository = arquivoRepository;
        this.socioRepository = socioRepository;
        this.auditService = auditService;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("Diretório de armazenamento criado/verificado: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            logger.error("Erro ao criar diretório de armazenamento: {}", this.fileStorageLocation, ex);
            throw new BusinessException(
                "FILE_STORAGE_INIT_ERROR",
                "Não foi possível criar o diretório de armazenamento de arquivos"
            );
        }
    }
    
    /**
     * Faz upload de múltiplos arquivos para um sócio específico.
     * Valida cada arquivo antes de armazenar e persiste os metadados no banco.
     * 
     * @param socioId ID do sócio proprietário dos arquivos
     * @param files Array de arquivos para upload
     * @return Lista de entidades Arquivo criadas
     * @throws ResourceNotFoundException se o sócio não existir
     * @throws BusinessException se houver erro de validação ou armazenamento
     */
    @Transactional
    public List<Arquivo> uploadArquivos(Long socioId, MultipartFile[] files) {
        logger.info("Iniciando upload de {} arquivo(s) para o sócio ID: {}", files.length, socioId);
        
        // Valida que o sócio existe
        Socio socio = socioRepository.findById(socioId)
            .orElseThrow(() -> new ResourceNotFoundException("Socio", "id", socioId));
        
        // Valida que há arquivos para upload
        if (files == null || files.length == 0) {
            throw new BusinessException("NO_FILES_PROVIDED", "Nenhum arquivo foi enviado");
        }
        
        // Verifica limite total de armazenamento do sócio
        Long tamanhoAtual = arquivoRepository.sumTamanhoBySocioId(socioId);
        long tamanhoNovosArquivos = calcularTamanhoTotal(files);
        
        if (tamanhoAtual + tamanhoNovosArquivos > ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO) {
            String mensagem = String.format(
                "Limite de armazenamento excedido. Atual: %s, Tentando adicionar: %s, Máximo: %s",
                ArquivoConstants.formatarTamanho(tamanhoAtual),
                ArquivoConstants.formatarTamanho(tamanhoNovosArquivos),
                ArquivoConstants.formatarTamanho(ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO)
            );
            logger.warn(mensagem);
            throw new BusinessException("STORAGE_LIMIT_EXCEEDED", mensagem);
        }
        
        List<Arquivo> arquivosSalvos = new ArrayList<>();
        
        // Processa cada arquivo
        for (MultipartFile file : files) {
            try {
                Arquivo arquivo = processarArquivo(socio, file);
                arquivosSalvos.add(arquivo);
                logger.info("Arquivo processado com sucesso: {} (ID: {})", arquivo.getNomeOriginal(), arquivo.getId());
            } catch (Exception ex) {
                logger.error("Erro ao processar arquivo: {}", file.getOriginalFilename(), ex);
                // Limpa arquivos já salvos em caso de erro
                limparArquivosSalvos(arquivosSalvos);
                throw new BusinessException(
                    "FILE_PROCESSING_ERROR",
                    "Erro ao processar arquivo: " + file.getOriginalFilename() + ". " + ex.getMessage()
                );
            }
        }
        
        logger.info("Upload concluído com sucesso. {} arquivo(s) salvos para o sócio ID: {}", 
                    arquivosSalvos.size(), socioId);
        
        return arquivosSalvos;
    }
    
    /**
     * Processa um único arquivo: valida, armazena no sistema de arquivos e persiste metadados.
     * 
     * @param socio Sócio proprietário do arquivo
     * @param file Arquivo para processar
     * @return Entidade Arquivo criada
     * @throws IOException se houver erro ao salvar o arquivo
     */
    private Arquivo processarArquivo(Socio socio, MultipartFile file) throws IOException {
        // Valida o arquivo
        validarArquivo(file);
        
        // Gera nome único para o arquivo
        String nomeOriginal = StringUtils.cleanPath(file.getOriginalFilename());
        String nomeArquivo = gerarNomeUnico(nomeOriginal);
        
        // Cria diretório específico do sócio
        Path diretorioSocio = criarDiretorioSocio(socio.getId());
        
        // Caminho completo do arquivo
        Path caminhoDestino = diretorioSocio.resolve(nomeArquivo);
        
        // Salva o arquivo no sistema de arquivos
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, caminhoDestino, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Arquivo salvo no sistema de arquivos: {}", caminhoDestino);
        }
        
        // Cria entidade Arquivo com metadados
        Arquivo arquivo = new Arquivo(
            socio,
            nomeOriginal,
            nomeArquivo,
            file.getContentType(),
            file.getSize(),
            caminhoDestino.toString()
        );
        
        // Persiste no banco de dados
        Arquivo savedArquivo = arquivoRepository.save(arquivo);
        
        // Log audit
        auditService.logCriacao("Arquivo", savedArquivo.getId(), savedArquivo);
        
        return savedArquivo;
    }
    
    /**
     * Valida um arquivo antes do upload.
     * Verifica se o arquivo não está vazio, se o tipo é permitido e se o tamanho está dentro do limite.
     * 
     * @param file Arquivo para validar
     * @throws BusinessException se a validação falhar
     */
    private void validarArquivo(MultipartFile file) {
        // Verifica se o arquivo está vazio
        if (file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", ArquivoConstants.ERRO_ARQUIVO_VAZIO);
        }
        
        // Valida o nome do arquivo
        String nomeOriginal = file.getOriginalFilename();
        if (nomeOriginal == null || nomeOriginal.contains("..")) {
            throw new BusinessException("INVALID_FILE_NAME", ArquivoConstants.ERRO_NOME_ARQUIVO_INVALIDO);
        }
        
        // Valida o tamanho do arquivo
        if (!ArquivoConstants.isTamanhoValido(file.getSize())) {
            String mensagem = String.format(
                "%s. Tamanho do arquivo: %s",
                ArquivoConstants.ERRO_ARQUIVO_MUITO_GRANDE,
                ArquivoConstants.formatarTamanho(file.getSize())
            );
            throw new BusinessException("FILE_TOO_LARGE", mensagem);
        }
        
        // Valida o tipo de conteúdo
        String contentType = file.getContentType();
        if (!validarTipoArquivo(contentType)) {
            throw new BusinessException("INVALID_FILE_TYPE", ArquivoConstants.ERRO_TIPO_NAO_PERMITIDO);
        }
        
        // Valida a extensão do arquivo
        if (!ArquivoConstants.isExtensaoPermitida(nomeOriginal)) {
            throw new BusinessException("INVALID_FILE_EXTENSION", ArquivoConstants.ERRO_TIPO_NAO_PERMITIDO);
        }
    }
    
    /**
     * Valida se o tipo de conteúdo do arquivo é permitido.
     * 
     * @param contentType Tipo MIME do arquivo
     * @return true se o tipo é permitido, false caso contrário
     */
    public boolean validarTipoArquivo(String contentType) {
        return ArquivoConstants.isTipoConteudoPermitido(contentType);
    }
    
    /**
     * Valida se o tamanho do arquivo está dentro do limite permitido.
     * 
     * @param tamanho Tamanho do arquivo em bytes
     * @return true se o tamanho é válido, false caso contrário
     */
    public boolean validarTamanhoArquivo(long tamanho) {
        return ArquivoConstants.isTamanhoValido(tamanho);
    }
    
    /**
     * Gera um nome único para o arquivo usando timestamp e UUID.
     * Formato: {timestamp}_{uuid}_{nomeOriginal}
     * 
     * @param nomeOriginal Nome original do arquivo
     * @return Nome único gerado
     */
    private String gerarNomeUnico(String nomeOriginal) {
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        // Remove caracteres especiais do nome original
        String nomeSeguro = nomeOriginal.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return String.format("%d_%s_%s", timestamp, uuid, nomeSeguro);
    }
    
    /**
     * Cria o diretório específico para armazenar arquivos de um sócio.
     * Estrutura: {uploadDir}/socios/{socioId}/
     * 
     * @param socioId ID do sócio
     * @return Path do diretório criado
     * @throws IOException se houver erro ao criar o diretório
     */
    private Path criarDiretorioSocio(Long socioId) throws IOException {
        Path diretorioSocio = this.fileStorageLocation
            .resolve(ArquivoConstants.DIRETORIO_BASE_UPLOAD)
            .resolve(socioId.toString());
        
        if (!Files.exists(diretorioSocio)) {
            Files.createDirectories(diretorioSocio);
            logger.debug("Diretório criado para sócio ID {}: {}", socioId, diretorioSocio);
        }
        
        return diretorioSocio;
    }
    
    /**
     * Calcula o tamanho total de um array de arquivos.
     * 
     * @param files Array de arquivos
     * @return Tamanho total em bytes
     */
    private long calcularTamanhoTotal(MultipartFile[] files) {
        long total = 0;
        for (MultipartFile file : files) {
            total += file.getSize();
        }
        return total;
    }
    
    /**
     * Lista todos os arquivos de um sócio específico.
     * 
     * @param socioId ID do sócio
     * @return Lista de arquivos do sócio
     */
    @Transactional(readOnly = true)
    public List<Arquivo> listarArquivos(Long socioId) {
        logger.debug("Listando arquivos do sócio ID: {}", socioId);
        
        // Verifica se o sócio existe
        if (!socioRepository.existsById(socioId)) {
            throw new ResourceNotFoundException("Socio", "id", socioId);
        }
        
        return arquivoRepository.findBySocioId(socioId);
    }
    
    /**
     * Busca um arquivo específico por ID.
     * 
     * @param id ID do arquivo
     * @return Entidade Arquivo
     * @throws ResourceNotFoundException se o arquivo não existir
     */
    @Transactional(readOnly = true)
    public Arquivo buscarArquivo(Long id) {
        logger.debug("Buscando arquivo ID: {}", id);
        
        return arquivoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Arquivo", "id", id));
    }
    
    /**
     * Faz download de um arquivo específico.
     * Retorna o arquivo como Resource para streaming.
     * 
     * @param id ID do arquivo
     * @return Resource contendo o arquivo
     * @throws ResourceNotFoundException se o arquivo não existir
     * @throws BusinessException se houver erro ao ler o arquivo
     */
    @Transactional(readOnly = true)
    public Resource downloadArquivo(Long id) {
        logger.debug("Iniciando download do arquivo ID: {}", id);
        
        Arquivo arquivo = buscarArquivo(id);
        
        try {
            Path caminhoArquivo = Paths.get(arquivo.getCaminhoArquivo());
            Resource resource = new UrlResource(caminhoArquivo.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                logger.info("Download do arquivo {} iniciado com sucesso", arquivo.getNomeOriginal());
                return resource;
            } else {
                logger.error("Arquivo não encontrado ou não legível: {}", caminhoArquivo);
                throw new BusinessException(
                    "FILE_NOT_FOUND_IN_STORAGE",
                    "Arquivo não encontrado no sistema de arquivos: " + arquivo.getNomeOriginal()
                );
            }
        } catch (IOException ex) {
            logger.error("Erro ao ler arquivo ID {}: {}", id, ex.getMessage(), ex);
            throw new BusinessException(
                "FILE_READ_ERROR",
                "Erro ao ler o arquivo: " + arquivo.getNomeOriginal()
            );
        }
    }
    
    /**
     * Exclui um arquivo do sistema.
     * Remove tanto o arquivo físico quanto o registro no banco de dados.
     * 
     * @param id ID do arquivo
     * @throws ResourceNotFoundException se o arquivo não existir
     * @throws BusinessException se houver erro ao excluir o arquivo físico
     */
    @Transactional
    public void excluirArquivo(Long id) {
        logger.info("Iniciando exclusão do arquivo ID: {}", id);
        
        Arquivo arquivo = buscarArquivo(id);
        Arquivo arquivoCopy = copiarArquivo(arquivo);
        
        // Remove o arquivo físico
        try {
            Path caminhoArquivo = Paths.get(arquivo.getCaminhoArquivo());
            Files.deleteIfExists(caminhoArquivo);
            logger.debug("Arquivo físico removido: {}", caminhoArquivo);
        } catch (IOException ex) {
            logger.error("Erro ao excluir arquivo físico ID {}: {}", id, ex.getMessage(), ex);
            throw new BusinessException(
                "FILE_DELETE_ERROR",
                "Erro ao excluir o arquivo físico: " + arquivo.getNomeOriginal()
            );
        }
        
        // Remove o registro do banco de dados
        arquivoRepository.delete(arquivo);
        
        // Log audit
        auditService.logExclusao("Arquivo", id, arquivoCopy);
        
        logger.info("Arquivo ID {} excluído com sucesso", id);
    }
    
    private Arquivo copiarArquivo(Arquivo original) {
        Arquivo copy = new Arquivo();
        copy.setId(original.getId());
        copy.setSocio(original.getSocio());
        copy.setNomeOriginal(original.getNomeOriginal());
        copy.setNomeArquivo(original.getNomeArquivo());
        copy.setTipoConteudo(original.getTipoConteudo());
        copy.setTamanho(original.getTamanho());
        copy.setCaminhoArquivo(original.getCaminhoArquivo());
        return copy;
    }
    
    /**
     * Limpa arquivos salvos em caso de erro durante upload múltiplo.
     * Remove tanto os arquivos físicos quanto os registros do banco.
     * 
     * @param arquivos Lista de arquivos para limpar
     */
    private void limparArquivosSalvos(List<Arquivo> arquivos) {
        logger.warn("Limpando {} arquivo(s) devido a erro no upload", arquivos.size());
        
        for (Arquivo arquivo : arquivos) {
            try {
                // Remove arquivo físico
                Path caminhoArquivo = Paths.get(arquivo.getCaminhoArquivo());
                Files.deleteIfExists(caminhoArquivo);
                
                // Remove registro do banco
                if (arquivo.getId() != null) {
                    arquivoRepository.delete(arquivo);
                }
            } catch (Exception ex) {
                logger.error("Erro ao limpar arquivo {}: {}", arquivo.getNomeOriginal(), ex.getMessage());
                // Continua limpando os outros arquivos mesmo se houver erro
            }
        }
    }
    
    /**
     * Obtém estatísticas de armazenamento de um sócio.
     * 
     * @param socioId ID do sócio
     * @return Objeto com estatísticas de armazenamento
     */
    @Transactional(readOnly = true)
    public ArquivoStatistics obterEstatisticas(Long socioId) {
        logger.debug("Obtendo estatísticas de armazenamento do sócio ID: {}", socioId);
        
        if (!socioRepository.existsById(socioId)) {
            throw new ResourceNotFoundException("Socio", "id", socioId);
        }
        
        long quantidadeArquivos = arquivoRepository.countBySocioId(socioId);
        Long tamanhoTotal = arquivoRepository.sumTamanhoBySocioId(socioId);
        long espacoDisponivel = ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO - tamanhoTotal;
        
        return new ArquivoStatistics(
            quantidadeArquivos,
            tamanhoTotal,
            espacoDisponivel,
            ArquivoConstants.TAMANHO_MAXIMO_TOTAL_POR_SOCIO
        );
    }
    
    /**
     * Classe interna para representar estatísticas de armazenamento.
     */
    public static class ArquivoStatistics {
        private final long quantidadeArquivos;
        private final long tamanhoTotal;
        private final long espacoDisponivel;
        private final long limiteTotal;
        
        public ArquivoStatistics(long quantidadeArquivos, long tamanhoTotal, 
                                long espacoDisponivel, long limiteTotal) {
            this.quantidadeArquivos = quantidadeArquivos;
            this.tamanhoTotal = tamanhoTotal;
            this.espacoDisponivel = espacoDisponivel;
            this.limiteTotal = limiteTotal;
        }
        
        public long getQuantidadeArquivos() {
            return quantidadeArquivos;
        }
        
        public long getTamanhoTotal() {
            return tamanhoTotal;
        }
        
        public long getEspacoDisponivel() {
            return espacoDisponivel;
        }
        
        public long getLimiteTotal() {
            return limiteTotal;
        }
        
        public String getTamanhoTotalFormatado() {
            return ArquivoConstants.formatarTamanho(tamanhoTotal);
        }
        
        public String getEspacoDisponivelFormatado() {
            return ArquivoConstants.formatarTamanho(espacoDisponivel);
        }
        
        public String getLimiteTotalFormatado() {
            return ArquivoConstants.formatarTamanho(limiteTotal);
        }
        
        public double getPercentualUtilizado() {
            return (tamanhoTotal * 100.0) / limiteTotal;
        }
    }
}
