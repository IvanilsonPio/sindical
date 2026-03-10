import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { errorInterceptor } from './error.interceptor';
import { ErrorService } from '../services/error.service';
import { ErrorCode, ErrorResponse } from '../models/error.model';

describe('ErrorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let router: Router;
  let errorService: ErrorService;
  let consoleErrorSpy: jasmine.Spy;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        ErrorService,
        {
          provide: Router,
          useValue: {
            navigate: jasmine.createSpy('navigate'),
            url: '/test'
          }
        }
      ]
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    errorService = TestBed.inject(ErrorService);
    
    consoleErrorSpy = spyOn(console, 'error');
  });

  afterEach(() => {
    httpMock.verify();
  });

  describe('Network Errors (status 0)', () => {
    it('should handle network errors with descriptive message', (done) => {
      const testUrl = '/api/test';

      httpClient.get(testUrl).subscribe({
        next: () => fail('should have failed with network error'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(0);
          expect(error.error.message).toBe('Erro ao comunicar com o servidor. Verifique sua conexão');
          expect(error.error.errorCode).toBe('NETWORK_ERROR');
          expect(consoleErrorSpy).toHaveBeenCalledWith('Network Error:', jasmine.objectContaining({
            url: testUrl,
            method: 'GET',
            message: 'Erro ao comunicar com o servidor. Verifique sua conexão'
          }));
          done();
        }
      });

      const req = httpMock.expectOne(testUrl);
      req.error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown Error' });
    });

    it('should log network error with timestamp', (done) => {
      httpClient.get('/api/test').subscribe({
        next: () => fail('should have failed'),
        error: () => {
          expect(consoleErrorSpy).toHaveBeenCalledWith('Network Error:', jasmine.objectContaining({
            timestamp: jasmine.any(String)
          }));
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.error(new ProgressEvent('error'), { status: 0 });
    });
  });

  describe('Server Errors (status 500+)', () => {
    it('should handle 500 Internal Server Error with descriptive message', (done) => {
      const testUrl = '/api/test';

      httpClient.get(testUrl).subscribe({
        next: () => fail('should have failed with server error'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(500);
          expect(error.error.message).toBe('Erro no servidor. Tente novamente mais tarde');
          expect(error.error.errorCode).toBe('SERVER_ERROR');
          expect(consoleErrorSpy).toHaveBeenCalledWith('Server Error:', jasmine.objectContaining({
            url: testUrl,
            status: 500
          }));
          done();
        }
      });

      const req = httpMock.expectOne(testUrl);
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
    });

    it('should handle 503 Service Unavailable', (done) => {
      httpClient.get('/api/test').subscribe({
        next: () => fail('should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(503);
          expect(error.error.message).toBe('Erro no servidor. Tente novamente mais tarde');
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.flush(null, { status: 503, statusText: 'Service Unavailable' });
    });

    it('should preserve backend error message for 500+ errors', (done) => {
      const backendError: ErrorResponse = {
        errorCode: 'DATABASE_ERROR',
        message: 'Erro ao acessar o banco de dados',
        timestamp: new Date().toISOString(),
        status: 500
      };

      httpClient.get('/api/test').subscribe({
        next: () => fail('should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(500);
          expect(error.error.message).toBe('Erro ao acessar o banco de dados');
          expect(error.error.errorCode).toBe('DATABASE_ERROR');
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.flush(backendError, { status: 500, statusText: 'Internal Server Error' });
    });

    it('should log server errors with structured information', (done) => {
      httpClient.post('/api/test', {}).subscribe({
        next: () => fail('should have failed'),
        error: () => {
          expect(consoleErrorSpy).toHaveBeenCalledWith('Server Error:', jasmine.objectContaining({
            url: '/api/test',
            method: 'POST',
            status: 502,
            timestamp: jasmine.any(String)
          }));
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.flush(null, { status: 502, statusText: 'Bad Gateway' });
    });
  });

  describe('Session Expired Errors', () => {
    it('should clear token and redirect to login on session expired', (done) => {
      const errorResponse: ErrorResponse = {
        errorCode: ErrorCode.SESSION_EXPIRED,
        message: 'Sua sessão expirou',
        timestamp: new Date().toISOString()
      };

      spyOn(localStorage, 'removeItem');

      httpClient.get('/api/test').subscribe({
        next: () => fail('should have failed'),
        error: () => {
          expect(localStorage.removeItem).toHaveBeenCalledWith('token');
          expect(router.navigate).toHaveBeenCalledWith(['/login'], {
            queryParams: { sessionExpired: 'true' }
          });
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.flush(errorResponse, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('Other HTTP Errors', () => {
    it('should log and re-throw 400 Bad Request errors', (done) => {
      const errorResponse: ErrorResponse = {
        errorCode: 'VALIDATION_ERROR',
        message: 'Dados inválidos',
        timestamp: new Date().toISOString()
      };

      httpClient.post('/api/test', {}).subscribe({
        next: () => fail('should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(400);
          expect(error.error.message).toBe('Dados inválidos');
          expect(consoleErrorSpy).toHaveBeenCalledWith('HTTP Error Intercepted:', jasmine.objectContaining({
            status: 400,
            errorCode: 'VALIDATION_ERROR'
          }));
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.flush(errorResponse, { status: 400, statusText: 'Bad Request' });
    });

    it('should log and re-throw 404 Not Found errors', (done) => {
      const errorResponse: ErrorResponse = {
        errorCode: 'SOCIO_NOT_FOUND',
        message: 'Sócio não encontrado',
        timestamp: new Date().toISOString()
      };

      httpClient.get('/api/socios/999').subscribe({
        next: () => fail('should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(404);
          expect(error.error.message).toBe('Sócio não encontrado');
          done();
        }
      });

      const req = httpMock.expectOne('/api/socios/999');
      req.flush(errorResponse, { status: 404, statusText: 'Not Found' });
    });

    it('should log and re-throw 409 Conflict errors', (done) => {
      const errorResponse: ErrorResponse = {
        errorCode: 'DUPLICATE_ENTRY',
        message: 'CPF já cadastrado',
        timestamp: new Date().toISOString()
      };

      httpClient.post('/api/socios', {}).subscribe({
        next: () => fail('should have failed'),
        error: (error: HttpErrorResponse) => {
          expect(error.status).toBe(409);
          expect(error.error.message).toBe('CPF já cadastrado');
          done();
        }
      });

      const req = httpMock.expectOne('/api/socios');
      req.flush(errorResponse, { status: 409, statusText: 'Conflict' });
    });
  });

  describe('Error Logging', () => {
    it('should include request URL in error logs', (done) => {
      const testUrl = '/api/socios/123';

      httpClient.get(testUrl).subscribe({
        next: () => fail('should have failed'),
        error: () => {
          expect(consoleErrorSpy).toHaveBeenCalledWith(jasmine.any(String), jasmine.objectContaining({
            url: testUrl
          }));
          done();
        }
      });

      const req = httpMock.expectOne(testUrl);
      req.flush(null, { status: 404, statusText: 'Not Found' });
    });

    it('should include request method in error logs', (done) => {
      httpClient.put('/api/socios/123', {}).subscribe({
        next: () => fail('should have failed'),
        error: () => {
          expect(consoleErrorSpy).toHaveBeenCalledWith(jasmine.any(String), jasmine.objectContaining({
            method: 'PUT'
          }));
          done();
        }
      });

      const req = httpMock.expectOne('/api/socios/123');
      req.flush(null, { status: 400, statusText: 'Bad Request' });
    });

    it('should include timestamp in error logs', (done) => {
      httpClient.get('/api/test').subscribe({
        next: () => fail('should have failed'),
        error: () => {
          expect(consoleErrorSpy).toHaveBeenCalledWith(jasmine.any(String), jasmine.objectContaining({
            timestamp: jasmine.any(String)
          }));
          done();
        }
      });

      const req = httpMock.expectOne('/api/test');
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
    });
  });
});
