import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { GlobalErrorHandler } from './global-error-handler';
import { ErrorCode } from '../../models/error.model';

describe('GlobalErrorHandler', () => {
  let errorHandler: GlobalErrorHandler;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        GlobalErrorHandler,
        { provide: Router, useValue: routerSpy }
      ]
    });

    errorHandler = TestBed.inject(GlobalErrorHandler);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should be created', () => {
    expect(errorHandler).toBeTruthy();
  });

  describe('HTTP Errors', () => {
    it('should handle session expired error', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.SESSION_EXPIRED,
          message: 'Session expired',
          timestamp: new Date().toISOString()
        },
        status: 401,
        statusText: 'Unauthorized'
      });

      spyOn(localStorage, 'removeItem');
      spyOn(console, 'error');
      spyOn(console, 'warn');

      // When
      errorHandler.handleError(error);

      // Then
      expect(localStorage.removeItem).toHaveBeenCalledWith('token');
      expect(router.navigate).toHaveBeenCalledWith(['/login'], {
        queryParams: { sessionExpired: 'true' }
      });
    });

    it('should handle access denied error', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.ACCESS_DENIED,
          message: 'Access denied',
          timestamp: new Date().toISOString()
        },
        status: 403,
        statusText: 'Forbidden'
      });

      spyOn(console, 'error');
      spyOn(console, 'warn');

      // When
      errorHandler.handleError(error);

      // Then
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('should handle invalid credentials error', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INVALID_CREDENTIALS,
          message: 'Invalid credentials',
          timestamp: new Date().toISOString()
        },
        status: 401,
        statusText: 'Unauthorized'
      });

      spyOn(console, 'error');

      // When
      errorHandler.handleError(error);

      // Then
      expect(console.error).toHaveBeenCalled();
      // Should not navigate (let login component handle it)
      expect(router.navigate).not.toHaveBeenCalled();
    });

    it('should handle network error (status 0)', () => {
      // Given
      const error = new HttpErrorResponse({
        error: null,
        status: 0,
        statusText: 'Unknown Error'
      });

      spyOn(console, 'error');

      // When
      errorHandler.handleError(error);

      // Then
      expect(console.error).toHaveBeenCalled();
    });

    it('should handle server error (status 500)', () => {
      // Given
      const error = new HttpErrorResponse({
        error: {
          errorCode: ErrorCode.INTERNAL_ERROR,
          message: 'Internal server error',
          timestamp: new Date().toISOString()
        },
        status: 500,
        statusText: 'Internal Server Error'
      });

      spyOn(console, 'error');

      // When
      errorHandler.handleError(error);

      // Then
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('Client Errors', () => {
    it('should handle JavaScript error', () => {
      // Given
      const error = new Error('JavaScript error occurred');
      spyOn(console, 'error');

      // When
      errorHandler.handleError(error);

      // Then
      expect(console.error).toHaveBeenCalledWith(
        'Client Error:',
        jasmine.objectContaining({
          message: 'JavaScript error occurred'
        })
      );
    });

    it('should handle TypeError', () => {
      // Given
      const error = new TypeError('Cannot read property of undefined');
      spyOn(console, 'error');

      // When
      errorHandler.handleError(error);

      // Then
      expect(console.error).toHaveBeenCalled();
    });
  });
});
