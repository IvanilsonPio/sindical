package com.sindicato.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.sindicato.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for centralized exception handling.
 * Provides standardized error responses and structured logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle authentication errors (invalid credentials).
     */
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            Exception ex, HttpServletRequest request) {
        
        logger.warn("Authentication failed: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.INVALID_CREDENTIALS.getCode(),
            ErrorCode.INVALID_CREDENTIALS.getMessage(),
            request.getRequestURI(),
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handle session expiration errors.
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpired(
            SessionExpiredException ex, HttpServletRequest request) {
        
        logger.warn("Session expired: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.SESSION_EXPIRED.getCode(),
            ErrorCode.SESSION_EXPIRED.getMessage(),
            request.getRequestURI(),
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Handle access denied errors.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.ACCESS_DENIED.getCode(),
            ErrorCode.ACCESS_DENIED.getMessage(),
            request.getRequestURI(),
            HttpStatus.FORBIDDEN.value()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    /**
     * Handle missing request header errors (e.g., missing Authorization header).
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(
            ServletRequestBindingException ex, HttpServletRequest request) {
        
        logger.warn("Request binding error: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        // If the error message mentions Authorization, return 401
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("authorization")) {
            ErrorResponse error = new ErrorResponse(
                ErrorCode.INVALID_CREDENTIALS.getCode(),
                "Token de autenticação não fornecido",
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        // For other binding errors, return 400
        ErrorResponse error = new ErrorResponse(
            ErrorCode.REQUIRED_FIELD_MISSING.getCode(),
            "Parâmetro ou cabeçalho obrigatório ausente",
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation failed - Path: {}", request.getRequestURI());
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ErrorResponse.FieldError(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue()
            ));
        }
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.INVALID_FORMAT.getCode(),
            "Erro de validação nos campos fornecidos",
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        error.setFieldErrors(fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle custom validation exceptions.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        
        logger.warn("Validation exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getFieldErrors().forEach((field, message) -> 
            fieldErrors.add(new ErrorResponse.FieldError(field, message))
        );
        
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        error.setFieldErrors(fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle duplicate entry errors.
     */
    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntry(
            DuplicateEntryException ex, HttpServletRequest request) {
        
        logger.warn("Duplicate entry: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.DUPLICATE_ENTRY.getCode(),
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.CONFLICT.value()
        );
        
        if (ex.getFieldName() != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("field", ex.getFieldName());
            details.put("value", ex.getFieldValue());
            error.setDetails(details);
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle resource not found errors.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.SOCIO_NOT_FOUND.getCode(),
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.NOT_FOUND.value()
        );
        
        Map<String, Object> details = new HashMap<>();
        details.put("resource", ex.getResourceName());
        details.put("field", ex.getFieldName());
        details.put("value", ex.getFieldValue());
        error.setDetails(details);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handle business logic errors.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        logger.warn("Business exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle file storage errors.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex, HttpServletRequest request) {
        
        logger.error("File storage error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Handle file size exceeded errors.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        logger.warn("File size exceeded - Path: {}", request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.FILE_TOO_LARGE.getCode(),
            ErrorCode.FILE_TOO_LARGE.getMessage(),
            request.getRequestURI(),
            HttpStatus.PAYLOAD_TOO_LARGE.value()
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }
    
    /**
     * Handle database integrity violations.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logger.error("Database integrity violation - Path: {}", request.getRequestURI(), ex);
        
        String message = "Erro de integridade de dados";
        String errorCode = ErrorCode.DATABASE_ERROR.getCode();
        
        // Try to provide more specific error message
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("duplicate key")) {
                message = "Entrada duplicada detectada";
                errorCode = ErrorCode.DUPLICATE_ENTRY.getCode();
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Operação viola integridade referencial";
            }
        }
        
        ErrorResponse error = new ErrorResponse(
            errorCode,
            message,
            request.getRequestURI(),
            HttpStatus.CONFLICT.value()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        logger.error("Unexpected error - Path: {}", request.getRequestURI(), ex);
        
        ErrorResponse error = new ErrorResponse(
            ErrorCode.INTERNAL_ERROR.getCode(),
            ErrorCode.INTERNAL_ERROR.getMessage(),
            request.getRequestURI(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
