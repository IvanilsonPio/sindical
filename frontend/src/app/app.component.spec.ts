import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { AuthService } from './services/auth.service';
import { BehaviorSubject } from 'rxjs';

describe('AppComponent', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let isAuthenticatedSubject: BehaviorSubject<boolean>;

  beforeEach(async () => {
    isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
    
    mockAuthService = jasmine.createSpyObj('AuthService', ['logout', 'getUser'], {
      isAuthenticated$: isAuthenticatedSubject.asObservable()
    });

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideAnimations(),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should have title "Sistema Sindicato Rural"', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('Sistema Sindicato Rural');
  });

  it('should show login view when not authenticated', () => {
    isAuthenticatedSubject.next(false);
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-container')).toBeNull();
  });

  it('should show main layout when authenticated', () => {
    mockAuthService.getUser.and.returnValue({ 
      nome: 'Test User', 
      token: 'test-token',
      refreshToken: 'refresh-token',
      username: 'testuser',
      success: true
    });
    isAuthenticatedSubject.next(true);
    
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-container')).toBeTruthy();
    expect(compiled.querySelector('.app-toolbar')).toBeTruthy();
  });

  it('should display user name when authenticated', () => {
    mockAuthService.getUser.and.returnValue({ 
      nome: 'João Silva', 
      token: 'test-token',
      refreshToken: 'refresh-token',
      username: 'joao',
      success: true
    });
    isAuthenticatedSubject.next(true);
    
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    
    const app = fixture.componentInstance;
    expect(app.userName).toBe('João Silva');
  });

  it('should call logout when logout button is clicked', () => {
    mockAuthService.getUser.and.returnValue({ 
      nome: 'Test User', 
      token: 'test-token',
      refreshToken: 'refresh-token',
      username: 'testuser',
      success: true
    });
    isAuthenticatedSubject.next(true);
    
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    
    app.logout();
    
    expect(mockAuthService.logout).toHaveBeenCalled();
  });
});
