package com.sindicato.exception;

/**
 * Exception thrown when file storage operations fail.
 */
public class FileStorageException extends RuntimeException {
    
    private final String errorCode;
    
    public FileStorageException(String message) {
        super(message);
        this.errorCode = ErrorCode.FILE_SYSTEM_ERROR.getCode();
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.FILE_SYSTEM_ERROR.getCode();
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
