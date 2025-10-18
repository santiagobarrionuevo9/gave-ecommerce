import { Routes } from '@angular/router';
import { ProductbrowseComponent } from './product/productbrowse/productbrowse.component';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', redirectTo: 'catalogo', pathMatch: 'full' },

  // catálogo (listado con filtros que ya hiciste)
  { path: 'catalogo', component: ProductbrowseComponent },
   // LOGIN (si no lo tenés ya)
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login.component').then(m => m.LoginComponent)
  },

  // detalle de producto (lo podés crear luego; acá dejo lazy)
  {
    path: 'producto/:slug',
    loadComponent: () =>
      import('./product/detailproduct/detailproduct.component')
        .then(m => m.DetailproductComponent)
  },
  // ADMIN — protegidos
  {
    path: 'admin/crear',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./product/createproduct/createproduct.component')
        .then(m => m.CreateproductComponent)
  },
  {
    path: 'admin/editar',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./product/editproduct/editproduct.component')
        .then(m => m.EditproductComponent)
  },

  // 404 básico
  { path: '**', redirectTo: 'catalogo' }
];
