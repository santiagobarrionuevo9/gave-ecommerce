import { Component } from '@angular/core';
import { OrderService } from '../../service/order.service';
import { CartService } from '../../service/cart.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cartorder',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './cartorder.component.html',
  styleUrl: './cartorder.component.css'
})
export class CartorderComponent {
  buyerEmail = '';
  buyerName  = '';
  buyerPhone = '';

  deliveryMethod: 'DELIVERY'|'PICKUP' = 'PICKUP';
  deliveryCost = 0;

  // Dirección (si DELIVERY)
  address = {
    street: '', number: '', apt: '', reference: '',
    city: '', province: '', postalCode: '', lat: null as number|null, lng: null as number|null
  };

  constructor(public cart: CartService, private orders: OrderService) {}

  itemsTotal() { return this.cart.snapshot.itemsTotal; }
  grandTotal() { return this.itemsTotal() + (this.deliveryMethod==='DELIVERY' ? this.deliveryCost : 0); }

  setQty(id: number, q: number) { this.cart.setQty(id, Number(q)); }
  remove(id: number) { this.cart.remove(id); }
  clear() { this.cart.clear(); }

  checkout() {
    if (!this.cart.snapshot.items.length) return alert('El carrito está vacío');
    if (!this.buyerEmail || !this.buyerName) return alert('Email y nombre son obligatorios');

    const dto: any = {
      buyerEmail: this.buyerEmail,
      buyerName: this.buyerName,
      buyerPhone: this.buyerPhone,
      deliveryMethod: this.deliveryMethod,
      deliveryCost: this.deliveryMethod==='DELIVERY' ? this.deliveryCost : 0,
      items: this.cart.snapshot.items.map(i => ({ productId: i.productId, quantity: i.quantity }))
    };
    if (this.deliveryMethod==='DELIVERY') dto.address = this.address;

    this.orders.createOrder(dto).subscribe({
      next: (o) => { alert('Pedido creado #'+o.id); this.cart.clear(); },
      error: (e) => { alert('No se pudo crear el pedido: ' + (e.error?.message || e.message)); }
    });
  }
}