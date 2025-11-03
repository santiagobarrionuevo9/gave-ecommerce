// src/app/guards/admin.guard.ts
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../service/auth.service';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const auth   = inject(AuthService);
  if (auth.hasRole('ADMIN')) return true;
  router.navigate(['/catalogo']);
  return false;
};
