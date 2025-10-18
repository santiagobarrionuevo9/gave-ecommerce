// src/app/guards/admin.guard.ts
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const role = localStorage.getItem('role');
  if (role === 'ADMIN') return true;
  router.navigate(['/catalogo']); // o a una página “403”
  return false;
};
