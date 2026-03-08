import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse, ERROR_MESSAGES } from '../models/error.model';

/**
 * Service for handling and formatting errors.
 */
@Injectable({
  providedIn: 'root'
})
export class ErrorService {

  /**
   * Extract user-friendly error message from HTTP error
   */
  getErrorMessage(error: HttpErrorResponse): string {
    if (!error.error) {
      return this.getDefaultErrorMessage(error.status);
    }

    const errorResponse = error.error as ErrorResponse;

    // Check if we have a mapped error message
    if (errorResponse.errorCode && ERROR_MESSAGES[errorResponse.errorCode]) {
      return ERROR_MESSAGES[errorResponse.errorCode];
    }

    // Return the message from backend
    if (errorResponse.message) {
      return errorResponse.message;
    }

    // Fallback to default message
    return this.getDefaultErrorMessage(error.status);
  }

  /**
   * Get field-specific error messages
   */
  getFieldErrors(error: HttpErrorResponse): Map<string, string> {
    const fieldErrors = new Map<string, string>();

    if (!error.error) {
      return fieldErrors;
    }

    const errorResponse = error.error as ErrorResponse;

    if (errorResponse.fieldErrors) {
      errorResponse.fieldErrors.forEach(fieldError => {
        fieldErrors.set(fieldError.field, fieldError.message);
      });
    }

    return fieldErrors;
  }

  /**
   * Check if error is a validation error
   */
  isValidationError(error: HttpErrorResponse): boolean {
    const errorResponse = error.error as ErrorResponse;
    return errorResponse?.fieldErrors !== undefined && errorResponse.fieldErrors.length > 0;
  }

  /**
   * Check if error is an authentication error
   */
  isAuthenticationError(error: HttpErrorResponse): boolean {
    const errorResponse = error.error as ErrorResponse;
    return errorResponse?.errorCode?.startsWith('AUTH') || false;
  }

  /**
   * Get default error message based on HTTP status code
   */
  private getDefaultErrorMessage(status: number): string {
    switch (status) {
      case 0:
        return 'Não foi possível conectar ao servidor. Verifique sua conexão.';
      case 400:
        return 'Requisição inválida. Verifique os dados fornecidos.';
      case 401:
        return 'Não autorizado. Faça login novamente.';
      case 403:
        return 'Acesso negado. Você não tem permissão para esta operação.';
      case 404:
        return 'Recurso não encontrado.';
      case 409:
        return 'Conflito. O recurso já existe.';
      case 413:
        return 'Arquivo muito grande.';
      case 500:
        return 'Erro interno do servidor. Tente novamente mais tarde.';
      case 503:
        return 'Serviço temporariamente indisponível.';
      default:
        return 'Ocorreu um erro inesperado. Tente novamente.';
    }
  }

  /**
   * Log error for debugging (in production, this could send to a logging service)
   */
  logError(error: Error | HttpErrorResponse, context?: string): void {
    const timestamp = new Date().toISOString();
    
    if (error instanceof HttpErrorResponse) {
      const errorResponse = error.error as ErrorResponse;
      console.error(`[${timestamp}] HTTP Error${context ? ` in ${context}` : ''}:`, {
        status: error.status,
        errorCode: errorResponse?.errorCode,
        message: errorResponse?.message,
        url: error.url
      });
    } else {
      console.error(`[${timestamp}] Client Error${context ? ` in ${context}` : ''}:`, {
        message: error.message,
        stack: error.stack
      });
    }
  }
}
