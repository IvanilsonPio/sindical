import { ErrorHandler, Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ErrorResponse, ErrorCode, ERROR_MESSAGES } from '../../models/error.model';

/**
 * Global error handler for Angular application.
 * Provides centralized error handling with user-friendly messages.
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private router = inject(Router);

  handleError(error: Error | HttpErrorResponse): void {
    if (error instanceof HttpErrorResponse) {
      // Server-side or network error
      this.handleHttpError(error);
    } else {
      // Client-side error
      this.handleClientError(error);
    }
  }

  /**
   * Handle HTTP errors from backend
   */
  private handleHttpError(error: HttpErrorResponse): void {
    const errorResponse = error.error as ErrorResponse;
    
    console.error('HTTP Error:', {
      status: error.status,
      errorCode: errorResponse?.errorCode,
      message: errorResponse?.message,
      url: error.url,
      timestamp: new Date().toISOString()
    });

    // Handle specific error codes
    if (errorResponse?.errorCode) {
      switch (errorResponse.errorCode) {
        case ErrorCode.SESSION_EXPIRED:
          this.handleSessionExpired();
          break;
        case ErrorCode.ACCESS_DENIED:
          this.handleAccessDenied();
          break;
        case ErrorCode.INVALID_CREDENTIALS:
          // Let the login component handle this
          break;
        default:
          this.showErrorNotification(errorResponse);
      }
    } else {
      // Handle errors without error code (network errors, etc.)
      this.handleNetworkError(error);
    }
  }

  /**
   * Handle client-side JavaScript errors
   */
  private handleClientError(error: Error): void {
    console.error('Client Error:', {
      message: error.message,
      stack: error.stack,
      timestamp: new Date().toISOString()
    });

    // Show generic error message to user
    this.showGenericError();
  }

  /**
   * Handle session expiration
   */
  private handleSessionExpired(): void {
    console.warn('Session expired, redirecting to login');
    localStorage.removeItem('token');
    this.router.navigate(['/login'], {
      queryParams: { sessionExpired: 'true' }
    });
  }

  /**
   * Handle access denied errors
   */
  private handleAccessDenied(): void {
    console.warn('Access denied');
    this.router.navigate(['/dashboard']);
    // In a real app, show a notification here
  }

  /**
   * Handle network errors (no response from server)
   */
  private handleNetworkError(error: HttpErrorResponse): void {
    if (error.status === 0) {
      console.error('Network error: Unable to connect to server');
      // In a real app, show a notification about network issues
    } else {
      console.error(`Server error: ${error.status} - ${error.statusText}`);
      // In a real app, show appropriate error notification
    }
  }

  /**
   * Show error notification to user
   */
  private showErrorNotification(errorResponse: ErrorResponse): void {
    const message = this.getUserFriendlyMessage(errorResponse);
    console.error('Error notification:', message);
    
    // In a real app, integrate with a notification service (e.g., MatSnackBar)
    // For now, we just log it
  }

  /**
   * Show generic error message
   */
  private showGenericError(): void {
    console.error('An unexpected error occurred');
    // In a real app, show a user-friendly notification
  }

  /**
   * Get user-friendly error message
   */
  private getUserFriendlyMessage(errorResponse: ErrorResponse): string {
    if (errorResponse.errorCode && ERROR_MESSAGES[errorResponse.errorCode]) {
      return ERROR_MESSAGES[errorResponse.errorCode];
    }
    return errorResponse.message || 'Ocorreu um erro inesperado';
  }
}
