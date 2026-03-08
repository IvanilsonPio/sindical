import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { AuthResponse } from '../../models/auth.model';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let activatedRoute: any;

  const mockAuthResponse: AuthResponse = {
    token: 'mock-jwt-token',
    refreshToken: 'mock-refresh-token',
    username: 'admin',
    nome: 'Administrador',
    success: true
  };

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const activatedRouteMock = {
      queryParams: of({})
    };

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        ReactiveFormsModule,
        HttpClientTestingModule,
        BrowserAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ]
    }).compileComponents();

    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    activatedRoute = TestBed.inject(ActivatedRoute);

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Validation', () => {
    it('should initialize with empty and invalid form', () => {
      expect(component.loginForm.valid).toBeFalsy();
      expect(component.loginForm.get('username')?.value).toBe('');
      expect(component.loginForm.get('password')?.value).toBe('');
    });

    it('should require username field', () => {
      const username = component.loginForm.get('username');
      expect(username?.hasError('required')).toBeTruthy();
      
      username?.setValue('admin');
      expect(username?.hasError('required')).toBeFalsy();
    });

    it('should require password field', () => {
      const password = component.loginForm.get('password');
      expect(password?.hasError('required')).toBeTruthy();
      
      password?.setValue('password123');
      expect(password?.hasError('required')).toBeFalsy();
    });

    it('should validate minimum length for username', () => {
      const username = component.loginForm.get('username');
      username?.setValue('ab');
      expect(username?.hasError('minlength')).toBeTruthy();
      
      username?.setValue('admin');
      expect(username?.hasError('minlength')).toBeFalsy();
    });

    it('should validate minimum length for password', () => {
      const password = component.loginForm.get('password');
      password?.setValue('123');
      expect(password?.hasError('minlength')).toBeTruthy();
      
      password?.setValue('1234');
      expect(password?.hasError('minlength')).toBeFalsy();
    });

    it('should be valid when all fields are filled correctly', () => {
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      expect(component.loginForm.valid).toBeTruthy();
    });
  });

  describe('Error Messages', () => {
    it('should return required error message', () => {
      const username = component.loginForm.get('username');
      username?.markAsTouched();
      expect(component.getErrorMessage('username')).toBe('Este campo é obrigatório');
    });

    it('should return minlength error message', () => {
      const username = component.loginForm.get('username');
      username?.setValue('ab');
      username?.markAsTouched();
      expect(component.getErrorMessage('username')).toBe('Mínimo de 3 caracteres');
    });

    it('should return empty string when no errors', () => {
      const username = component.loginForm.get('username');
      username?.setValue('admin');
      expect(component.getErrorMessage('username')).toBe('');
    });
  });

  describe('Login Functionality', () => {
    it('should not submit when form is invalid', () => {
      component.onSubmit();
      expect(authService.login).not.toHaveBeenCalled();
    });

    it('should mark fields as touched when submitting invalid form', () => {
      component.onSubmit();
      expect(component.loginForm.get('username')?.touched).toBeTruthy();
      expect(component.loginForm.get('password')?.touched).toBeTruthy();
    });

    it('should call authService.login with form values on valid submit', () => {
      authService.login.and.returnValue(of(mockAuthResponse));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      
      expect(authService.login).toHaveBeenCalledWith({
        username: 'admin',
        password: 'password123'
      });
    });

    it('should set loading to true during login', () => {
      authService.login.and.returnValue(of(mockAuthResponse));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      expect(component.loading).toBeTruthy();
    });

    it('should navigate to dashboard on successful login', () => {
      authService.login.and.returnValue(of(mockAuthResponse));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      
      expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    });

    it('should display error message on 401 unauthorized', () => {
      const error = { status: 401 };
      authService.login.and.returnValue(throwError(() => error));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'wrongpassword'
      });
      
      component.onSubmit();
      
      expect(component.error).toBe('Credenciais inválidas. Verifique seu usuário e senha.');
      expect(component.loading).toBeFalsy();
    });

    it('should display error message on 403 forbidden', () => {
      const error = { status: 403 };
      authService.login.and.returnValue(throwError(() => error));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      
      expect(component.error).toBe('Acesso negado. Sua conta pode estar inativa.');
      expect(component.loading).toBeFalsy();
    });

    it('should display connection error on network failure', () => {
      const error = { status: 0 };
      authService.login.and.returnValue(throwError(() => error));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      
      expect(component.error).toBe('Não foi possível conectar ao servidor. Verifique sua conexão.');
      expect(component.loading).toBeFalsy();
    });

    it('should display generic error for other errors', () => {
      const error = { status: 500 };
      authService.login.and.returnValue(throwError(() => error));
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      
      expect(component.error).toBe('Erro ao fazer login. Tente novamente mais tarde.');
      expect(component.loading).toBeFalsy();
    });

    it('should clear error and session messages on new submit', () => {
      authService.login.and.returnValue(of(mockAuthResponse));
      
      component.error = 'Previous error';
      component.sessionExpiredMessage = 'Session expired';
      
      component.loginForm.setValue({
        username: 'admin',
        password: 'password123'
      });
      
      component.onSubmit();
      
      expect(component.error).toBe('');
      expect(component.sessionExpiredMessage).toBe('');
    });
  });

  describe('Session Expiration', () => {
    it('should display inactivity message when redirected with expired=true and reason=inactivity', () => {
      activatedRoute.queryParams = of({ expired: 'true', reason: 'inactivity' });
      component.ngOnInit();
      
      expect(component.sessionExpiredMessage).toBe(
        'Sua sessão expirou devido à inatividade. Por favor, faça login novamente.'
      );
    });

    it('should display generic expiration message when redirected with expired=true', () => {
      activatedRoute.queryParams = of({ expired: 'true' });
      component.ngOnInit();
      
      expect(component.sessionExpiredMessage).toBe(
        'Sua sessão expirou. Por favor, faça login novamente.'
      );
    });

    it('should not display message when not redirected from expiration', () => {
      activatedRoute.queryParams = of({});
      component.ngOnInit();
      
      expect(component.sessionExpiredMessage).toBe('');
    });
  });

  describe('Password Visibility', () => {
    it('should initialize with password hidden', () => {
      expect(component.hidePassword).toBeTruthy();
    });

    it('should toggle password visibility', () => {
      expect(component.hidePassword).toBeTruthy();
      
      component.togglePasswordVisibility();
      expect(component.hidePassword).toBeFalsy();
      
      component.togglePasswordVisibility();
      expect(component.hidePassword).toBeTruthy();
    });
  });
});
