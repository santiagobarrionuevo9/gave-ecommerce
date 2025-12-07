import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterModule, RouterOutlet } from '@angular/router';
import { CartService } from './service/cart.service';
import { AuthService } from './service/auth.service';
import { ProductstateService } from './service/productstate.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { Productdto } from './interface/product/productdto';
import { ProductService } from './service/product.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet,RouterLink, CommonModule,  RouterModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  lowStockProducts: Productdto[] = [];
lowStockCount = 0;
  title = 'gave-frontend';
  lowStock = signal<Productdto[]>([]);
  year = new Date().getFullYear();

  private cart = inject(CartService);
  auth = inject(AuthService);
  
  // 1) Pasamos el observable state$ a signal
  cartState = toSignal(this.cart.state$, {
    initialValue: this.cart.snapshot   // 👈 usamos tu snapshot como valor inicial
  });

  // 2) Ahora sí, computed depende de un signal y se recalcula solo
  cartCount = computed(() =>
    this.cartState().items.reduce((a, b) => a + b.quantity, 0)
  );
  constructor(
  // lo que ya tengas...
  private productService: ProductService,
  private router: Router
) {}

  

ngOnInit(): void {
  if (this.isAdmin()) {
    this.loadLowStock();
    // opcional: refrescar cada X tiempo
    // interval(60000).subscribe(() => this.loadLowStock());
  }
}

loadLowStock() {
  this.productService.getLowStock().subscribe({
    next: list => this.lowStock.set(list),
    error: () => this.lowStock.set([]),
  });
}

  isLoggedIn() { return this.auth.isLoggedIn; }
  isAdmin()    { return this.auth.hasRole('ADMIN'); }
  displayName(): string {
    return localStorage.getItem('email') || 'Usuario';
  }

  logout() { this.auth.logout(); }

  goToEditProduct(id: number) {
  this.router.navigate(['/admin/editar/', id]);
}

}
