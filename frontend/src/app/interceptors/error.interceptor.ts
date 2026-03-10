import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ErrorResponse, ErrorCode } from '../models/error.model';
import { ErrorService } from '../services/error.service';

/**
 * HTTP Error Interceptor for handling API errors globally.
 * Intercepts HTTP errors and provides consistent error handling.
 * 
 * Handles:
 * - Network errors (status 0)
 * - Server errors (status 500+)
 * - Authentication errors
 * - Provides descriptive error messages
 * 
 * Requirements: 6.3, 6.4
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const errorService = inject(ErrorService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const errorResponse = error.error as ErrorResponse;

      // Handle network errors (status 0)
      if (error.status === 0) {
        console.error('Network Error:', {
          url: req.url,
          method: req.method,
          message: 'Erro ao comunicar com o servidor. Verifique sua conexão',
          timestamp: new Date().toISOString()
        });
        
        // Create a user-friendly error response
        const networkError = new HttpErrorResponse({
          error: {
            errorCode: 'NETWORK_ERROR',
            message: 'Erro ao comunicar com o servidor. Verifique sua conexão',
            timestamp: new Date().toISOString(),
            status: 0
          } as ErrorResponse,
          status: 0,
          statusText: 'Network Error',
          url: req.url || undefined
        });
        
        return throwError(() => networkError);
      }

      // Handle server errors (500+)
      if (error.status >= 500) {
        console.error('Server Error:', {
          url: req.url,
          method: req.method,
          status: error.status,
          errorCode: errorResponse?.errorCode,
          message: errorResponse?.message || 'Erro interno do servidor',
          timestamp: new Date().toISOString()
        });
        
        // Provide descriptive error message for server errors
        const serverError = new HttpErrorResponse({
          error: {
            errorCode: errorResponse?.errorCode || 'SERVER_ERROR',
            message: errorResponse?.message || 'Erro no servidor. Tente novamente mais tarde',
            timestamp: errorResponse?.timestamp || new Date().toISOString(),
            status: error.status,
            path: errorResponse?.path
          } as ErrorResponse,
          status: error.status,
          statusText: error.statusText,
          url: req.url || undefined
        });
        
        return throwError(() => serverError);
      }

      // Log structured error information for other errors
      console.error('HTTP Error Intercepted:', {
        url: req.url,
        method: req.method,
        status: error.status,
        errorCode: errorResponse?.errorCode,
        message: errorResponse?.message,
        timestamp: new Date().toISOString()
      });

      // Handle specific error scenarios
      if (errorResponse?.errorCode === ErrorCode.SESSION_EXPIRED) {
        // Clear token and redirect to login
        localStorage.removeItem('token');
        router.navigate(['/login'], {
          queryParams: { sessionExpired: 'true' }
        });
      }

      // Re-throw the error for component-level handling
      return throwError(() => error);
    })
  );
};
