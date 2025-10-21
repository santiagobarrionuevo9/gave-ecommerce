import { Routes } from '@angular/router';
import { ProductbrowseComponent } from './product/productbrowse/productbrowse.component';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', redirectTo: 'catalogo', pathMatch: 'full' },
  { path: 'catalogo', component: ProductbrowseComponent },
  { path: 'producto/:slug', loadComponent: () => import('./product/detailproduct/detailproduct.component').then(m => m.DetailproductComponent) },

  // auth
  { path: 'login',    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'forgot',   loadComponent: () => import('./auth/forgot/forgot.component').then(m => m.ForgotComponent) },
  { path: 'reset',    loadComponent: () => import('./auth/reset/reset.component').then(m => m.ResetComponent) },

  // admin
  { path: 'admin/crear',  canActivate: [adminGuard], loadComponent: () => import('./product/createproduct/createproduct.component').then(m => m.CreateproductComponent) },
  { path: 'admin/editar', canActivate: [adminGuard], loadComponent: () => import('./product/editproduct/editproduct.component').then(m => m.EditproductComponent) },

  { path: '**', redirectTo: 'catalogo' }
];
