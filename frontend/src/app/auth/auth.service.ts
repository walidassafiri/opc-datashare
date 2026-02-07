import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

interface AuthResponse { token: string; tokenType?: string }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'datashare_token';

  constructor(private http: HttpClient) {}

  register(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', { email, password }).pipe(
      tap(res => this.saveToken(res.token))
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(
      tap(res => this.saveToken(res.token))
    );
  }

  saveToken(token: string) { localStorage.setItem(this.TOKEN_KEY, token); }
  getToken(): string | null { return localStorage.getItem(this.TOKEN_KEY); }
  isAuthenticated(): boolean { return !!this.getToken(); }

  logoutLocal() { localStorage.removeItem(this.TOKEN_KEY); }

  logout(): Observable<any> {
    // call backend logout endpoint, clear token locally regardless of result
    return this.http.post('/api/auth/logout', {}).pipe(
      // always clear token locally after call
      tap(() => this.logoutLocal())
    );
  }
}
