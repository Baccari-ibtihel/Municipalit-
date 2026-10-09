import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AgentService {
  private baseUrl = 'http://localhost:8085/api/agent';

  constructor(private http: HttpClient, private authService: AuthService) { }

  getAllRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/requests`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  updateRequestStatus(id: number, status: string, missingDocNotes?: string): Observable<any> {
    let params = new HttpParams().set('status', status);
    if (missingDocNotes) {
      params = params.set('missingDocNotes', missingDocNotes);
    }
    return this.http.put<any>(`${this.baseUrl}/requests/${id}/status`, {}, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  getAllReports(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/reports`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  updateReportStatus(id: number, status: string): Observable<any> {
    const params = new HttpParams().set('status', status);
    return this.http.put<any>(`${this.baseUrl}/reports/${id}/status`, {}, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  getAllContracts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/contracts`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  updateContractStatus(id: number, status: string, missingDocNotes?: string): Observable<any> {
    let params = new HttpParams().set('status', status);
    if (missingDocNotes) {
      params = params.set('missingDocNotes', missingDocNotes);
    }
    return this.http.put<any>(`${this.baseUrl}/contracts/${id}/status`, {}, {
      headers: this.authService.getAuthHeaders(),
      params
    });
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  /** Increment queue (citizen arrived at baladiya) */
  incrementQueue(baladiayaId: number): Observable<any> {
    return this.http.post<any>(
      `http://localhost:8085/api/agent/baladiya/${baladiayaId}/queue/increment`,
      {},
      { headers: this.authService.getAuthHeaders() }
    );
  }

  /** Decrement queue (citizen was served) */
  decrementQueue(baladiayaId: number): Observable<any> {
    return this.http.post<any>(
      `http://localhost:8085/api/agent/baladiya/${baladiayaId}/queue/decrement`,
      {},
      { headers: this.authService.getAuthHeaders() }
    );
  }
}

