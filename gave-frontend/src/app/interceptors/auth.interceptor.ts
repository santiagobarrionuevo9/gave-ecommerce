// src/app/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment.prod';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token =
    localStorage.getItem('token') ||
    sessionStorage.getItem('token');

  const isApiRequest = req.url.includes('/api/');

  if (token && isApiRequest) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError(err => {
      if (err.status === 401 || err.status === 403) {
        console.warn('⚠️ Token inválido o expirado (mobile común)');
        // NO limpiar storage automáticamente
        // Dejá que el usuario vuelva a loguear solo si hace falta
      }
      return throwError(() => err);
    })
  );
};