import { Component } from '@angular/core';
import { OrderService } from '../../service/order.service';
import { CartService } from '../../service/cart.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-cartorder',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './cartorder.component.html',
  styleUrl: './cartorder.component.css'
})
export class CartorderComponent {
  buyerEmail = localStorage.getItem('email') || '';
  buyerName  = '';
  buyerPhone = '';

  deliveryMethod: 'DELIVERY'|'PICKUP' = 'PICKUP';
  deliveryCost = 0;

  submitting = false;

  address = {
    street: '', number: '', apt: '', reference: '',
    city: '', province: '', postalCode: '', lat: null as number|null, lng: null as number|null
  };

  constructor(
    public cart: CartService,
    private orders: OrderService,
    private router: Router,
    private auth: AuthService        // 👈 inyección del AuthService
  ) {}

  // 👇 getter para usarlo fácil en el HTML
  get isLoggedIn() {
    return this.auth.isLoggedIn;
  }

  itemsTotal() { return this.cart.snapshot.itemsTotal; }
  grandTotal() { 
    return this.itemsTotal() + (this.deliveryMethod === 'DELIVERY' ? this.deliveryCost : 0); 
  }

  setQty(id: number, q: number) { this.cart.setQty(id, Number(q)); }
  remove(id: number) { this.cart.remove(id); }
  clear() { this.cart.clear(); }

  async checkout() {
    // 🔒 1) chequeo de login
    if (!this.isLoggedIn) {
      const res = await Swal.fire({
        icon: 'info',
        title: 'Iniciá sesión',
        text: 'Debés iniciar sesión para confirmar tu pedido.',
        showCancelButton: true,
        confirmButtonText: 'Ir a iniciar sesión',
        cancelButtonText: 'Seguir en el carrito'
      });

      if (res.isConfirmed) {
        this.router.navigate(
          ['/login'],
          { queryParams: { returnUrl: this.router.url } } // 👈 para volver al carrito luego
        );
      }

      return; // 👈 NO sigue con la creación del pedido
    }

    // ✅ 2) resto de validaciones normales
    if (!this.cart.snapshot.items.length) {
      await Swal.fire({ icon: 'warning', title: 'Carrito vacío', text: 'Agrega productos para continuar.' });
      return;
    }
    if (!this.buyerEmail || !this.buyerName) {
      await Swal.fire({ icon: 'info', title: 'Datos incompletos', text: 'Email y nombre son obligatorios.' });
      return;
    }

    const dto: any = {
      buyerEmail: this.buyerEmail,
      buyerName: this.buyerName,
      buyerPhone: this.buyerPhone,
      deliveryMethod: this.deliveryMethod,
      deliveryCost: this.deliveryMethod === 'DELIVERY' ? this.deliveryCost : 0,
      items: this.cart.snapshot.items.map(i => ({
        productId: i.productId,
        quantity: i.quantity
      }))
    };
    if (this.deliveryMethod === 'DELIVERY') dto.address = this.address;

    this.submitting = true;
    this.orders.createOrder(dto).subscribe({
      next: async (o) => {
        this.submitting = false;
        this.cart.clear();
        const res = await Swal.fire({
          icon: 'success',
          title: '¡Pedido confirmado!',
          html: `Tu número de pedido es <b>#${o.id}</b>.`,
          confirmButtonText: 'Ir al catálogo'
        });
        if (res.isConfirmed) this.router.navigate(['/catalogo']);
      },
      error: async (e) => {
        this.submitting = false;
        const msg = e?.error?.message || e?.message || 'Ocurrió un error inesperado.';
        await Swal.fire({
          icon: 'error',
          title: 'No se pudo crear el pedido',
          text: msg,
          confirmButtonText: 'Entendido'
        });
      }
    });
  }
}