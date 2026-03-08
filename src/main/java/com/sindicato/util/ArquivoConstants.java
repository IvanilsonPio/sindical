package com.sindicato.util;

import java.util.Set;

/**
 * Constantes para validação e configuração de arquivos.
 * Define tipos de arquivo permitidos e limites de tamanho.
 */
public final class ArquivoConstants {
    
    // Previne instanciação
    private ArquivoConstants() {
        throw new UnsupportedOperationException("Classe de constantes não pode ser instanciada");
    }
    
    // ============================================
    // Tamanhos de arquivo
    // ============================================
    
    /**
     * Tamanho máximo permitido para um arquivo individual: 10 MB
     */
    public static final long TAMANHO_MAXIMO_ARQUIVO = 10 * 1024 * 1024; // 10 MB em bytes
    
    /**
     * Tamanho máximo total de arquivos por sócio: 100 MB
     */
    public static final long TAMANHO_MAXIMO_TOTAL_POR_SOCIO = 100 * 1024 * 1024; // 100 MB em bytes
    
    // ============================================
    // Tipos de conteúdo permitidos
    // ============================================
    
    /**
     * Tipos MIME permitidos para upload de arquivos.
     * Inclui documentos PDF, imagens comuns e documentos do Microsoft Office.
     */
    public static final Set<String> TIPOS_CONTEUDO_PERMITIDOS = Set.of(
        // Documentos PDF
        "application/pdf",
        
        // Imagens
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp",
        
        // Documentos Microsoft Office
        "application/msword", // .doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.ms-excel", // .xls
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-powerpoint", // .ppt
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
        
        // Documentos de texto
        "text/plain",
        "text/csv",
        
        // Arquivos compactados
        "application/zip",
        "application/x-rar-compressed",
        "application/x-7z-compressed"
    );
    
    /**
     * Extensões de arquivo permitidas (para validação adicional).
     */
    public static final Set<String> EXTENSOES_PERMITIDAS = Set.of(
        // Documentos
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv",
        
        // Imagens
        "jpg", "jpeg", "png", "gif", "bmp", "webp",
        
        // Compactados
        "zip", "rar", "7z"
    );
    
    // ============================================
    // Mensagens de erro
    // ============================================
    
    public static final String ERRO_ARQUIVO_MUITO_GRANDE = 
        "Arquivo excede o tamanho máximo permitido de 10 MB";
    
    public static final String ERRO_TIPO_NAO_PERMITIDO = 
        "Tipo de arquivo não permitido. Tipos aceitos: PDF, imagens (JPG, PNG, GIF), " +
        "documentos Office (Word, Excel, PowerPoint), arquivos de texto e compactados (ZIP, RAR)";
    
    public static final String ERRO_LIMITE_TOTAL_EXCEDIDO = 
        "Limite total de armazenamento por sócio excedido (máximo: 100 MB)";
    
    public static final String ERRO_ARQUIVO_VAZIO = 
        "Arquivo está vazio ou não foi enviado corretamente";
    
    public static final String ERRO_NOME_ARQUIVO_INVALIDO = 
        "Nome do arquivo contém caracteres inválidos";
    
    // ============================================
    // Configurações de armazenamento
    // ============================================
    
    /**
     * Diretório base para armazenamento de arquivos.
     * Será concatenado com o ID do sócio para organização.
     */
    public static final String DIRETORIO_BASE_UPLOAD = "uploads/socios";
    
    /**
     * Padrão para geração de nomes únicos de arquivo.
     * Formato: {timestamp}_{uuid}_{nomeOriginal}
     */
    public static final String PADRAO_NOME_ARQUIVO = "%d_%s_%s";
    
    // ============================================
    // Métodos utilitários
    // ============================================
    
    /**
     * Verifica se o tipo de conteúdo é permitido.
     * 
     * @param tipoConteudo Tipo MIME do arquivo
     * @return true se o tipo é permitido, false caso contrário
     */
    public static boolean isTipoConteudoPermitido(String tipoConteudo) {
        return tipoConteudo != null && TIPOS_CONTEUDO_PERMITIDOS.contains(tipoConteudo.toLowerCase());
    }
    
    /**
     * Verifica se a extensão do arquivo é permitida.
     * 
     * @param nomeArquivo Nome do arquivo com extensão
     * @return true se a extensão é permitida, false caso contrário
     */
    public static boolean isExtensaoPermitida(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return false;
        }
        
        String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();
        return EXTENSOES_PERMITIDAS.contains(extensao);
    }
    
    /**
     * Verifica se o tamanho do arquivo está dentro do limite permitido.
     * 
     * @param tamanho Tamanho do arquivo em bytes
     * @return true se o tamanho é válido, false caso contrário
     */
    public static boolean isTamanhoValido(long tamanho) {
        return tamanho > 0 && tamanho <= TAMANHO_MAXIMO_ARQUIVO;
    }
    
    /**
     * Formata o tamanho do arquivo em formato legível (KB, MB).
     * 
     * @param tamanhoBytes Tamanho em bytes
     * @return String formatada (ex: "2.5 MB", "150 KB")
     */
    public static String formatarTamanho(long tamanhoBytes) {
        if (tamanhoBytes < 1024) {
            return tamanhoBytes + " B";
        } else if (tamanhoBytes < 1024 * 1024) {
            return String.format("%.2f KB", tamanhoBytes / 1024.0);
        } else {
            return String.format("%.2f MB", tamanhoBytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Extrai a extensão de um nome de arquivo.
     * 
     * @param nomeArquivo Nome do arquivo
     * @return Extensão do arquivo (sem o ponto) ou string vazia se não houver extensão
     */
    public static String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase();
    }
}
