// src/app/service/adminusers.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment.prod';

export interface AdminUser {
  id: number;
  email: string;
  fullName: string;
  priceAccess: boolean;
}

@Injectable({ providedIn: 'root' })
export class AdminUsersService {
  private readonly base = environment.apiBase + '/api/admin/users';

  constructor(private http: HttpClient) {}

  list(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(this.base);
  }

  setPriceAccess(id: number, value: boolean): Observable<void> {
    const params = new HttpParams().set('value', String(value));
    return this.http.patch<void>(`${this.base}/${id}/price-access`, null, { params });
  }
}