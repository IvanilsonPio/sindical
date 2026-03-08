package com.sindicato.exception;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.sindicato.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleInvalidCredentials_ShouldReturnUnauthorized() {
        // Given
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCredentials(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS.getCode());
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    void handleSessionExpired_ShouldReturnUnauthorized() {
        // Given
        SessionExpiredException exception = new SessionExpiredException("Session expired");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleSessionExpired(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.SESSION_EXPIRED.getCode());
    }

    @Test
    void handleAccessDenied_ShouldReturnForbidden() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED.getCode());
    }

    @Test
    void handleDuplicateEntry_ShouldReturnConflict() {
        // Given
        DuplicateEntryException exception = new DuplicateEntryException("cpf", "123.456.789-00");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateEntry(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_ENTRY.getCode());
        assertThat(response.getBody().getDetails()).containsEntry("field", "cpf");
    }

    @Test
    void handleResourceNotFound_ShouldReturnNotFound() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Socio", "id", 1L);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.SOCIO_NOT_FOUND.getCode());
        assertThat(response.getBody().getDetails()).containsEntry("resource", "Socio");
    }

    @Test
    void handleBusinessException_ShouldReturnBadRequest() {
        // Given
        BusinessException exception = new BusinessException("BUS002", "Payment already exists");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("BUS002");
    }

    @Test
    void handleFileStorageException_ShouldReturnInternalServerError() {
        // Given
        FileStorageException exception = new FileStorageException("File storage error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileStorageException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.FILE_SYSTEM_ERROR.getCode());
    }

    @Test
    void handleMaxUploadSizeExceeded_ShouldReturnPayloadTooLarge() {
        // Given
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(10000000);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxUploadSizeExceeded(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.FILE_TOO_LARGE.getCode());
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturnConflict() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Duplicate key violation");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.DATABASE_ERROR.getCode());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
    }
}
