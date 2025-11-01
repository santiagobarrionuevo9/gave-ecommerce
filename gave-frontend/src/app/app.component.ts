import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CartService } from './service/cart.service';

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
  cartCount = computed(() => this.cart.snapshot.items.reduce((a,b)=>a+b.quantity,0));
}
