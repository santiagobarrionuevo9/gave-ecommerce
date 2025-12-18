import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginReq } from '../interface/auth/loginreq';
import { AuthResponse } from '../interface/auth/authresponse';
import { tap } from 'rxjs';
import { RegisterReq } from '../interface/auth/registerreq';
import { ForgotPasswordRequest } from '../interface/auth/forgotpasswordrequest';
import { ResetPasswordRequest } from '../interface/auth/resetpasswordrequest';
import { environment } from '../../environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private base = environment.apiBase + '/api';

  private readonly TOKEN_KEY = 'token';
  private readonly ROLE_KEY  = 'role';
  private readonly EMAIL_KEY = 'email';

  constructor(private http: HttpClient) {}

  login(body: LoginReq){
    return this.http.post<AuthResponse>(`${this.base}/auth/login`, body)
      .pipe(tap(res => this.store(res)));
  }

  register(body: RegisterReq){
    return this.http.post<AuthResponse>(`${this.base}/auth/register`, body)
      .pipe(tap(res => this.store(res)));
  }

  // ----- PASSWORD FLOW -----
  forgotPassword(body: ForgotPasswordRequest) {
    return this.http.post<void>(`${this.base}/auth/forgot`, body);
  }

  resetPassword(body: ResetPasswordRequest) {
    return this.http.post<void>(`${this.base}/auth/reset`, body);
  }

  // ================= STORAGE =================

  private store(res: AuthResponse){
    // Local (persistente)
    localStorage.setItem(this.TOKEN_KEY, res.token);
    localStorage.setItem(this.ROLE_KEY, res.role);
    localStorage.setItem(this.EMAIL_KEY, res.email);

    // Session (backup para mobile)
    sessionStorage.setItem(this.TOKEN_KEY, res.token);
    sessionStorage.setItem(this.ROLE_KEY, res.role);
    sessionStorage.setItem(this.EMAIL_KEY, res.email);
  }

  logout(){
    localStorage.clear();
    sessionStorage.clear();
  }

  // ================= GETTERS =================

  get token(){
    return (
      localStorage.getItem(this.TOKEN_KEY) ||
      sessionStorage.getItem(this.TOKEN_KEY)
    );
  }

  get role(){
    return (
      (localStorage.getItem(this.ROLE_KEY) ||
       sessionStorage.getItem(this.ROLE_KEY)) as 'ADMIN'|'CLIENT'|null
    );
  }

  get email(){
    return (
      localStorage.getItem(this.EMAIL_KEY) ||
      sessionStorage.getItem(this.EMAIL_KEY)
    );
  }

  get isLoggedIn(){ return !!this.token; }

  hasRole(r: 'ADMIN'|'CLIENT'){ return this.role === r; }
}