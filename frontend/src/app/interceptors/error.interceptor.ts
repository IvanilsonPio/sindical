import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ErrorResponse, ErrorCode } from '../models/error.model';

/**
 * HTTP Error Interceptor for handling API errors.
 * Intercepts HTTP errors and provides consistent error handling.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const errorResponse = error.error as ErrorResponse;

      // Log structured error information
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
