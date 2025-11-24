import { Routes } from '@angular/router';
import { ProductbrowseComponent } from './product/productbrowse/productbrowse.component';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: '', redirectTo: 'catalogo', pathMatch: 'full' },
  { path: 'catalogo', component: ProductbrowseComponent },
  {path: 'historia', loadComponent: () => import('./introduccion/introduccion.component').then(m => m.IntroduccionComponent) },
  { path: 'producto/:slug', loadComponent: () => import('./product/detailproduct/detailproduct.component').then(m => m.DetailproductComponent) },

  // auth
  { path: 'login',    loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'forgot',   loadComponent: () => import('./auth/forgot/forgot.component').then(m => m.ForgotComponent) },
  { path: 'reset',    loadComponent: () => import('./auth/reset/reset.component').then(m => m.ResetComponent) },
  // routes.ts
  { path: 'carrito', loadComponent: () => import('./order/cartorder/cartorder.component').then(m => m.CartorderComponent) },
  { path: 'mis-pedidos', canActivate: [authGuard], loadComponent: () => import('./order/myorder/myorder.component').then(m => m.MyorderComponent) },


  // admin
  { path: 'admin/crear',  canActivate: [adminGuard], loadComponent: () => import('./product/createproduct/createproduct.component').then(m => m.CreateproductComponent) },
  { path: 'admin/editar', canActivate: [adminGuard], loadComponent: () => import('./product/editproduct/editproduct.component').then(m => m.EditproductComponent) },
  // NUEVA RUTA CON ID
  { path: 'admin/editar/:id', canActivate: [adminGuard],loadComponent: () => import('./product/editproduct/editproduct.component').then(m => m.EditproductComponent)},
  // src/app/app.routes.ts
  { path: 'admin/stock', canActivate: [adminGuard], loadComponent: () => import('./product/stockadmin/stockadmin.component').then(m => m.StockadminComponent) },
  { path: 'admin/categorias', canActivate: [adminGuard], loadComponent: () => import('./product/typeadmin/typeadmin.component').then(m => m.TypeadminComponent) },

   // ADMIN pedidos
  { path: 'admin/pedidos', canActivate: [adminGuard], loadComponent: () => import('./order/adminorderstatus/adminorderstatus.component').then(m => m.AdminorderstatusComponent) },
  { path: 'admin/pedidos/:id', canActivate: [adminGuard], loadComponent: () => import('./order/admindetailorder/admindetailorder.component').then(m => m.AdmindetailorderComponent) },
  
  { path: '**', redirectTo: 'catalogo' },
  
];
