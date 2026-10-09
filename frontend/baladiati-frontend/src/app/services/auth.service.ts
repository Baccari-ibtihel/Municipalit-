import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8085/api/auth';
  public currentLang: 'ar' | 'fr' = 'ar';
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.currentLang = (localStorage.getItem('lang') as 'ar' | 'fr') || 'ar';
    this.loadCurrentUser();
  }

  toggleLanguage(): void {
    this.currentLang = this.currentLang === 'ar' ? 'fr' : 'ar';
    localStorage.setItem('lang', this.currentLang);
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user, { responseType: 'text' });
  }

  registerAgent(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register-agent`, data, { responseType: 'text' });
  }

  activateAgent(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/activate-agent`, data, { responseType: 'text' });
  }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        if (res && res.token) {
          localStorage.setItem('token', res.token);
          localStorage.setItem('role', res.role);
          localStorage.setItem('username', res.name);  // AuthResponse returns 'name'
          this.loadCurrentUser();
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }

  private loadCurrentUser(): void {
    if (this.isLoggedIn()) {
      this.currentUserSubject.next({
        username: this.getUsername(),
        role: this.getRole(),
        token: this.getToken()
      });
    } else {
      this.currentUserSubject.next(null);
    }
  }
}
