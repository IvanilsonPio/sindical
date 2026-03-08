package com.sindicato.exception;

/**
 * Standardized error codes for the system.
 * Each error code follows the pattern: CATEGORY + NUMBER
 */
public enum ErrorCode {
    // Authentication errors (AUTH001-003)
    INVALID_CREDENTIALS("AUTH001", "Credenciais inválidas"),
    SESSION_EXPIRED("AUTH002", "Sessão expirada"),
    ACCESS_DENIED("AUTH003", "Acesso negado"),
    
    // Validation errors (VAL001-003)
    REQUIRED_FIELD_MISSING("VAL001", "Campo obrigatório ausente"),
    INVALID_FORMAT("VAL002", "Formato inválido"),
    DUPLICATE_ENTRY("VAL003", "Entrada duplicada"),
    
    // Business logic errors (BUS001-003)
    SOCIO_NOT_FOUND("BUS001", "Sócio não encontrado"),
    PAYMENT_ALREADY_EXISTS("BUS002", "Pagamento já existe para este período"),
    FILE_TOO_LARGE("BUS003", "Arquivo excede tamanho máximo permitido"),
    
    // System errors (SYS001-003)
    DATABASE_ERROR("SYS001", "Erro de banco de dados"),
    FILE_SYSTEM_ERROR("SYS002", "Erro no sistema de arquivos"),
    INTERNAL_ERROR("SYS003", "Erro interno do servidor");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
