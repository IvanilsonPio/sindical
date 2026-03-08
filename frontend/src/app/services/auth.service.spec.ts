import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { LoginRequest, AuthResponse } from '../models/auth.model';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  const mockLoginRequest: LoginRequest = {
    username: 'admin',
    password: 'password123'
  };

  const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    refreshToken: 'mock-refresh-token',
    username: 'admin',
    nome: 'Administrator',
    success: true
  };

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Clear localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('login', () => {
    it('should authenticate user with valid credentials and store token', (done) => {
      service.login(mockLoginRequest).subscribe(response => {
        expect(response).toEqual(mockAuthResponse);
        expect(service.getToken()).toBe(mockAuthResponse.token);
        expect(service.isAuthenticated()).toBe(true);
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockLoginRequest);
      req.flush(mockAuthResponse);
    });

    it('should update isAuthenticated$ observable on successful login', (done) => {
      service.isAuthenticated$.subscribe(isAuth => {
        if (isAuth) {
          expect(isAuth).toBe(true);
          done();
        }
      });

      service.login(mockLoginRequest).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
    });

    it('should store user data in localStorage', (done) => {
      service.login(mockLoginRequest).subscribe(() => {
        const storedUser = service.getUser();
        expect(storedUser).toEqual(mockAuthResponse);
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
    });

    it('should handle login failure', (done) => {
      const errorMessage = 'Invalid credentials';

      service.login(mockLoginRequest).subscribe({
        next: () => fail('should have failed'),
        error: (error) => {
          expect(error.status).toBe(401);
          expect(service.isAuthenticated()).toBe(false);
          done();
        }
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush({ message: errorMessage }, { status: 401, statusText: 'Unauthorized' });
    });
  });

  describe('logout', () => {
    beforeEach((done) => {
      service.login(mockLoginRequest).subscribe(() => done());
      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
    });

    it('should clear token from localStorage', () => {
      expect(service.getToken()).toBeTruthy();
      service.logout();
      expect(service.getToken()).toBeNull();
    });

    it('should clear user data from localStorage', () => {
      expect(service.getUser()).toBeTruthy();
      service.logout();
      expect(service.getUser()).toBeNull();
    });

    it('should update isAuthenticated to false', () => {
      expect(service.isAuthenticated()).toBe(true);
      service.logout();
      expect(service.isAuthenticated()).toBe(false);
    });

    it('should update isAuthenticated$ observable', (done) => {
      let callCount = 0;
      service.isAuthenticated$.subscribe(isAuth => {
        callCount++;
        if (callCount === 2) { // First call is true from login, second is false from logout
          expect(isAuth).toBe(false);
          done();
        }
      });

      service.logout();
    });
  });

  describe('isAuthenticated', () => {
    it('should return false when no token exists', () => {
      expect(service.isAuthenticated()).toBe(false);
    });

    it('should return true when valid token exists', (done) => {
      service.login(mockLoginRequest).subscribe(() => {
        expect(service.isAuthenticated()).toBe(true);
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
    });

    it('should return false and logout when session expired due to inactivity', fakeAsync(() => {
      // Login first
      service.login(mockLoginRequest).subscribe();
      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
      tick();

      expect(service.isAuthenticated()).toBe(true);

      // Simulate 31 minutes of inactivity (more than 30 minute timeout)
      const lastActivity = Date.now() - (31 * 60 * 1000);
      localStorage.setItem('last_activity', lastActivity.toString());

      expect(service.isAuthenticated()).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/login'], {
        queryParams: { expired: 'true', reason: 'inactivity' }
      });
    }));
  });

  describe('getToken', () => {
    it('should return null when no token stored', () => {
      expect(service.getToken()).toBeNull();
    });

    it('should return stored token', (done) => {
      service.login(mockLoginRequest).subscribe(() => {
        expect(service.getToken()).toBe(mockAuthResponse.token);
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
    });
  });

  describe('getUser', () => {
    it('should return null when no user stored', () => {
      expect(service.getUser()).toBeNull();
    });

    it('should return stored user data', (done) => {
      service.login(mockLoginRequest).subscribe(() => {
        const user = service.getUser();
        expect(user).toEqual(mockAuthResponse);
        done();
      });

      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
    });
  });

  describe('session timeout', () => {
    it('should automatically logout after 30 minutes of inactivity', fakeAsync(() => {
      // Login
      service.login(mockLoginRequest).subscribe();
      const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
      req.flush(mockAuthResponse);
      tick();

      expect(service.isAuthenticated()).toBe(true);

      // Simulate 31 minutes passing without activity
      const lastActivity = Date.now() - (31 * 60 * 1000);
      localStorage.setItem('last_activity', lastActivity.toString());

      // Trigger the inactivity check (runs every minute)
      tick(60000);

      expect(service.isAuthenticated()).toBe(false);
      expect(router.navigate).toHaveBeenCalledWith(['/login'], {
        queryParams: { expired: 'true', reason: 'inactivity' }
      });
    }));
  });
});
