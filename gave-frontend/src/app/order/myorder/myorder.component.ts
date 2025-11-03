import { Component, inject, OnInit, signal } from '@angular/core';
import { OrderService } from '../../service/order.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { OrderDTO } from '../../interface/order/types';
import { Pages } from '../../interface/product/pages';

@Component({
  selector: 'app-myorder',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './myorder.component.html',
  styleUrl: './myorder.component.css'
})
export class MyorderComponent implements OnInit {
  private auth = inject(AuthService);
  private ordersApi = inject(OrderService);

  orders: OrderDTO[] = [];
  loading = signal(false);
  error: string | null = null;

  ngOnInit() {
    const email = this.auth?.role ? localStorage.getItem('email') : null;
    if (!email) {
      this.error = 'Necesitás iniciar sesión para ver tus pedidos.';
      return;
    }
    this.fetch(email);
  }

  private fetch(email: string) {
    this.loading.set(true);
    this.error = null;
    this.ordersApi.myOrders(email, 0, 20).subscribe({
      next: (p: Pages<OrderDTO>) => { this.orders = p.content; this.loading.set(false); },
      error: (e) => {
        this.error = e?.error?.message || 'No se pudieron cargar tus pedidos.';
        this.loading.set(false);
      }
    });
  }
}
