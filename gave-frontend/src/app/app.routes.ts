import { Routes } from '@angular/router';
import { ProductbrowseComponent } from './product/productbrowse/productbrowse.component';

export const routes: Routes = [
    { path: '', redirectTo: 'catalogo', pathMatch: 'full' },

  // catálogo (listado con filtros que ya hiciste)
  { path: 'catalogo', component: ProductbrowseComponent },

  // detalle de producto (lo podés crear luego; acá dejo lazy)
  {
    path: 'producto/:slug',
    loadComponent: () =>
      import('./product/detailproduct/detailproduct.component')
        .then(m => m.DetailproductComponent)
  },
  {
    path: 'admin/crear',   // o el path que prefieras
    loadComponent: () => import('./product/createproduct/createproduct.component')
      .then(m => m.CreateproductComponent)
  },

  // 404 básico
  { path: '**', redirectTo: 'catalogo' }
];
