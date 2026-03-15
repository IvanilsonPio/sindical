import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';
import { LoginRequest, AuthResponse } from '../models/auth.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';
  private readonly LAST_ACTIVITY_KEY = 'last_activity';
  private readonly INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds
  private apiUrl = `${environment.apiUrl}/auth`;
  
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  private inactivityTimer: any;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.initInactivityMonitoring();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        this.setToken(response.token);
        this.setUser(response);
        this.updateLastActivity();
        this.isAuthenticatedSubject.next(true);
        this.startInactivityTimer();
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.LAST_ACTIVITY_KEY);
    this.isAuthenticatedSubject.next(false);
    this.stopInactivityTimer();
  }

  isAuthenticated(): boolean {
    if (!this.hasToken()) {
      return false;
    }
    
    // Check if session has expired due to inactivity
    if (this.isSessionExpired()) {
      this.logout();
      this.router.navigate(['/login'], { 
        queryParams: { expired: 'true', reason: 'inactivity' } 
      });
      return false;
    }
    
    return true;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUser(): AuthResponse | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  isAdmin(): boolean {
    return this.getUser()?.role === 'ADMIN';
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setUser(user: AuthResponse): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  private updateLastActivity(): void {
    localStorage.setItem(this.LAST_ACTIVITY_KEY, Date.now().toString());
  }

  private getLastActivity(): number {
    const lastActivity = localStorage.getItem(this.LAST_ACTIVITY_KEY);
    return lastActivity ? parseInt(lastActivity, 10) : 0;
  }

  private isSessionExpired(): boolean {
    const lastActivity = this.getLastActivity();
    if (!lastActivity) {
      return false;
    }
    
    const now = Date.now();
    const timeSinceLastActivity = now - lastActivity;
    return timeSinceLastActivity > this.INACTIVITY_TIMEOUT;
  }

  private initInactivityMonitoring(): void {
    // Monitor user activity
    const events = ['mousedown', 'keydown', 'scroll', 'touchstart', 'click'];
    
    events.forEach(event => {
      document.addEventListener(event, () => {
        if (this.isAuthenticated()) {
          this.updateLastActivity();
          this.resetInactivityTimer();
        }
      }, true);
    });

    // Start timer if already authenticated
    if (this.hasToken()) {
      this.startInactivityTimer();
    }
  }

  private startInactivityTimer(): void {
    this.stopInactivityTimer();
    
    this.inactivityTimer = setInterval(() => {
      if (this.isSessionExpired()) {
        this.logout();
        this.router.navigate(['/login'], { 
          queryParams: { expired: 'true', reason: 'inactivity' } 
        });
      }
    }, 60000); // Check every minute
  }

  private stopInactivityTimer(): void {
    if (this.inactivityTimer) {
      clearInterval(this.inactivityTimer);
      this.inactivityTimer = null;
    }
  }

  private resetInactivityTimer(): void {
    this.startInactivityTimer();
  }
}
