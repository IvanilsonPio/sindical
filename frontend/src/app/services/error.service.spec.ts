import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorService } from './error.service';
import { ErrorCode, ERROR_MESSAGES } from '../models/error.model';

describe('ErrorService', () => {
  let service: ErrorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ErrorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getErrorMessage', () => {
    it('should return mapped error message for known error code', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INVALID_CREDENTIALS,
          message: 'Invalid credentials',
          timestamp: new Date().toISOString()
        },
        status: 401
      });

      // When
      const message = service.getErrorMessage(error);

      // Then
      expect(message).toBe(ERROR_MESSAGES[ErrorCode.INVALID_CREDENTIALS]);
    });

    it('should return backend message when no mapped message exists', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: 'CUSTOM_ERROR',
          message: 'Custom error message',
          timestamp: new Date().toISOString()
        },
        status: 400
      });

      // When
      const message = service.getErrorMessage(error);

      // Then
      expect(message).toBe('Custom error message');
    });

    it('should return default message for network error', () => {
      // Given
      const error = new HttpErrorResponse({
        error: null,
        status: 0
      });

      // When
      const message = service.getErrorMessage(error);

      // Then
      expect(message).toContain('conectar ao servidor');
    });

    it('should return default message for 404', () => {
      // Given
      const error = new HttpErrorResponse({
        error: null,
        status: 404
      });

      // When
      const message = service.getErrorMessage(error);

      // Then
      expect(message).toContain('não encontrado');
    });
  });

  describe('getFieldErrors', () => {
    it('should extract field errors from error response', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INVALID_FORMAT,
          message: 'Validation failed',
          fieldErrors: [
            { field: 'nome', message: 'Nome é obrigatório' },
            { field: 'cpf', message: 'CPF inválido' }
          ],
          timestamp: new Date().toISOString()
        },
        status: 400
      });

      // When
      const fieldErrors = service.getFieldErrors(error);

      // Then
      expect(fieldErrors.size).toBe(2);
      expect(fieldErrors.get('nome')).toBe('Nome é obrigatório');
      expect(fieldErrors.get('cpf')).toBe('CPF inválido');
    });

    it('should return empty map when no field errors', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INTERNAL_ERROR,
          message: 'Internal error',
          timestamp: new Date().toISOString()
        },
        status: 500
      });

      // When
      const fieldErrors = service.getFieldErrors(error);

      // Then
      expect(fieldErrors.size).toBe(0);
    });
  });

  describe('isValidationError', () => {
    it('should return true for validation errors', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INVALID_FORMAT,
          message: 'Validation failed',
          fieldErrors: [
            { field: 'nome', message: 'Nome é obrigatório' }
          ],
          timestamp: new Date().toISOString()
        },
        status: 400
      });

      // When
      const isValidation = service.isValidationError(error);

      // Then
      expect(isValidation).toBe(true);
    });

    it('should return false for non-validation errors', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INTERNAL_ERROR,
          message: 'Internal error',
          timestamp: new Date().toISOString()
        },
        status: 500
      });

      // When
      const isValidation = service.isValidationError(error);

      // Then
      expect(isValidation).toBe(false);
    });
  });

  describe('isAuthenticationError', () => {
    it('should return true for authentication errors', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INVALID_CREDENTIALS,
          message: 'Invalid credentials',
          timestamp: new Date().toISOString()
        },
        status: 401
      });

      // When
      const isAuth = service.isAuthenticationError(error);

      // Then
      expect(isAuth).toBe(true);
    });

    it('should return false for non-authentication errors', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.SOCIO_NOT_FOUND,
          message: 'Socio not found',
          timestamp: new Date().toISOString()
        },
        status: 404
      });

      // When
      const isAuth = service.isAuthenticationError(error);

      // Then
      expect(isAuth).toBe(false);
    });
  });

  describe('logError', () => {
    it('should log HTTP error with context', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INTERNAL_ERROR,
          message: 'Internal error',
          timestamp: new Date().toISOString()
        },
        status: 500,
        url: '/api/socios'
      });

      spyOn(console, 'error');

      // When
      service.logError(error, 'SocioService');

      // Then
      expect(console.error).toHaveBeenCalledWith(
        jasmine.stringContaining('HTTP Error in SocioService'),
        jasmine.objectContaining({
          status: 500,
          errorCode: ErrorCode.INTERNAL_ERROR
        })
      );
    });

    it('should log client error', () => {
      // Given
      const error = new Error('JavaScript error');
      spyOn(console, 'error');

      // When
      service.logError(error);

      // Then
      expect(console.error).toHaveBeenCalledWith(
        jasmine.stringContaining('Client Error'),
        jasmine.objectContaining({
          message: 'JavaScript error'
        })
      );
    });
  });
});
