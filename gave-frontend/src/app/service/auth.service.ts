import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginReq } from '../interface/auth/loginreq';
import { AuthResponse } from '../interface/auth/authresponse';
import { tap } from 'rxjs';
import { RegisterReq } from '../interface/auth/registerreq';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private base = 'http://localhost:9011/api'; // o '/api' si usás proxy

  constructor(private http: HttpClient) {}

  login(body: LoginReq){
    return this.http.post<AuthResponse>(`${this.base}/auth/login`, body).pipe(
      tap(res => this.store(res))
    );
  }
  register(body: RegisterReq){
    return this.http.post<AuthResponse>(`${this.base}/auth/register`, body).pipe(
      tap(res => this.store(res))
    );
  }

  private store(res: AuthResponse){
    localStorage.setItem('token', res.token);
    localStorage.setItem('role', res.role);
    localStorage.setItem('email', res.email);
  }
  logout(){
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
  }
  get token(){ return localStorage.getItem('token'); }
  get role(){ return (localStorage.getItem('role') as 'ADMIN'|'CLIENT'|null); }
  get isLoggedIn(){ return !!this.token; }
  hasRole(r: 'ADMIN'|'CLIENT'){ return this.role === r; }
}