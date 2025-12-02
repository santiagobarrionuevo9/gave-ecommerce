// src/app/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment.prod';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');

  // Solo agregamos Authorization si la request va a nuestra API
  const isApiRequest = req.url.startsWith(environment.apiBase);

  if (token && isApiRequest) {
    req = req.clone({
      setHeaders: { 
        Authorization: `Bearer ${token}` 
      }
    });
  }

  return next(req);
};
