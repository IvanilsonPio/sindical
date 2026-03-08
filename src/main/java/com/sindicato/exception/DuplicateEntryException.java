package com.sindicato.exception;

/**
 * Exception thrown when attempting to create a duplicate entry.
 */
public class DuplicateEntryException extends RuntimeException {
    
    private final String fieldName;
    private final String fieldValue;
    
    public DuplicateEntryException(String fieldName, String fieldValue) {
        super(String.format("Duplicate entry for %s: '%s'", fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public DuplicateEntryException(String message) {
        super(message);
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public String getFieldValue() {
        return fieldValue;
    }
}