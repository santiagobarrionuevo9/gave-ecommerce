// src/app/guards/auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../service/auth.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const auth   = inject(AuthService);
  if (auth.isLoggedIn) return true;
  router.navigate(['/login'], { queryParams: { returnUrl: location.pathname }});
  return false;
};

