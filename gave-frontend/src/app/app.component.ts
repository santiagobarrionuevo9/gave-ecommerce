import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CartService } from './service/cart.service';
import { AuthService } from './service/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet,RouterLink, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  year = new Date().getFullYear();

  private cart = inject(CartService);
  auth = inject(AuthService);

  // contador carrito
  cartCount = computed(() => this.cart.snapshot.items.reduce((a, b) => a + b.quantity, 0));

  // helpers para template (usando tu AuthService basado en localStorage)
  isLoggedIn() { return this.auth.isLoggedIn; }
  isAdmin()    { return this.auth.hasRole('ADMIN'); }
  displayName(): string {
    return localStorage.getItem('email') || 'Usuario';
  }

  logout() { this.auth.logout(); }
}
