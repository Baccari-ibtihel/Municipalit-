import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CitizenService {
  private baseUrl = 'http://localhost:8085/api/citizen';
  private uploadUrl = 'http://localhost:8085/api/upload';
  private publicUrl = 'http://localhost:8085/api/public';

  constructor(private http: HttpClient, private authService: AuthService) { }

  getRequests(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/requests`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  createRequest(requestData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/requests`, requestData, {
      headers: this.authService.getAuthHeaders()
    });
  }

  getReports(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/reports`, {
      headers: this.authService.getAuthHeaders()
    });
  }

  createReport(reportData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/reports`, reportData, {
      headers: this.authService.getAuthHeaders()
    });
  }

  uploadFile(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    
    const token = this.authService.getToken();
    const headers = {
      'Authorization': token ? `Bearer ${token}` : ''
    };

    return this.http.post<any>(this.uploadUrl, formData, { headers });
  }

  /** Get list of nearest baladiyat sorted by distance to user */
  getNearbyBaladiyat(lat: number, lng: number, limit: number = 5): Observable<any[]> {
    return this.http.get<any[]>(`${this.publicUrl}/baladiyat/nearby`, {
      params: { lat: lat.toString(), lng: lng.toString(), limit: limit.toString() }
    });
  }

  /** Get nearest baladiya to given coordinates — no auth needed */
  getNearestBaladiya(lat: number, lng: number): Observable<any> {
    return this.http.get<any>(`${this.publicUrl}/nearest-baladiya`, {
      params: { lat: lat.toString(), lng: lng.toString() }
    });
  }

  /** Get live queue count for a baladiya — no auth needed */
  getQueueStatus(id: number): Observable<any> {
    return this.http.get<any>(`${this.publicUrl}/baladiya/${id}/queue`);
  }
}

