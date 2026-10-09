import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class OfficiantService {
  private baseUrl = 'http://localhost:8085/api/officiant';

  constructor(private http: HttpClient, private authService: AuthService) { }

  getMyContracts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/contracts`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  createContract(contractData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/contracts`, contractData, {
      headers: this.authService.getAuthHeaders()
    });
  }
}
