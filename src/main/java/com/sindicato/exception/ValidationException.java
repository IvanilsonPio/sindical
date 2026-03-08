package com.sindicato.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> fieldErrors;
    private final String errorCode;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
        this.errorCode = ErrorCode.INVALID_FORMAT.getCode();
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
        this.errorCode = ErrorCode.INVALID_FORMAT.getCode();
    }
    
    public ValidationException(String errorCode, String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
        this.errorCode = errorCode;
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
