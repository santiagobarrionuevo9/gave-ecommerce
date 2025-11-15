import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CartService } from './service/cart.service';
import { AuthService } from './service/auth.service';
import { ProductstateService } from './service/productstate.service';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet,RouterLink, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'gave-frontend';
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

  isLoggedIn() { return this.auth.isLoggedIn; }
  isAdmin()    { return this.auth.hasRole('ADMIN'); }
  displayName(): string {
    return localStorage.getItem('email') || 'Usuario';
  }

  logout() { this.auth.logout(); }
}
