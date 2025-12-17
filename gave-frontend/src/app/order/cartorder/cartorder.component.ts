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
  termsAccepted = false;
  deliveryMethod: 'DELIVERY' | 'PICKUP' = 'PICKUP';
  deliveryCost = 0; // siempre 0

  submitting = false;

  // Dirección
  address = {
    street: '',
    number: '',
    apt: '',
    reference: '',
    city: '',
    province: '',
    postalCode: '',
    lat: null as number | null,
    lng: null as number | null
  };

  // 🔢 Monto mínimo requerido para poder confirmar
  readonly MIN_TOTAL = 100000;

  constructor(
    public cart: CartService,
    private orders: OrderService,
    private router: Router,
    private auth: AuthService
  ) {}

  // ✅ getter para saber si está logueado
  get isLoggedIn(): boolean {
    return this.auth.isLoggedIn;
  }

  // 💰 total de ítems del carrito (sin envío)
  itemsTotal(): number {
    return this.cart.snapshot.itemsTotal;
  }

  // 💰 total general (ahora = subtotal, envío siempre 0)
  grandTotal(): number {
    return this.itemsTotal();
  }

  // 👉 ¿alcanza el monto mínimo?
  get meetsMinAmount(): boolean {
    return this.grandTotal() >= this.MIN_TOTAL;
  }

  // 👉 ¿puede confirmar? (logueado + monto mínimo + no enviando)
  get canConfirm(): boolean {
  return this.isLoggedIn && this.meetsMinAmount && this.termsAccepted && !this.submitting;
}


  setQty(id: number, q: number) {
    this.cart.setQty(id, Number(q));
  }

  remove(id: number) {
    this.cart.remove(id);
  }

  clear() {
  this.cart.clear();
  this.termsAccepted = false;
}

  private isValidWhatsappPhone(phone: string): boolean {
  if (!phone) return false;

  // dejo solo dígitos
  const digits = phone.replace(/\D/g, '');

  // no permitir que empiece con 0 ni con 15
  if (digits.startsWith('0') || digits.startsWith('15')) {
    return false;
  }

  // largo razonable: ej. 351 + 7 dígitos -> 10 mínimo
  if (digits.length < 10 || digits.length > 12) {
    return false;
  }

  return true;
}

private isValidEmail(email: string): boolean {
  if (!email) return false;
  const trimmed = email.trim();

  // Regex simple pero efectiva para formato de email
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(trimmed);
}



  async checkout() {
    // 1) Chequeo de login
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
          { queryParams: { returnUrl: this.router.url } }
        );
      }
      return;
    }

    // 2) Chequeo de monto mínimo
    if (!this.meetsMinAmount) {
      await Swal.fire({
        icon: 'warning',
        title: 'Monto mínimo no alcanzado',
        text: `El total del pedido debe ser de al menos $ ${this.MIN_TOTAL.toLocaleString('es-AR')} para confirmar la compra.`
      });
      return;
    }
    // 2.5) Chequeo de Términos
    if (!this.termsAccepted) {
      await Swal.fire({
        icon: 'warning',
        title: 'Falta aceptar términos',
        text: 'Para confirmar el pedido, debés aceptar los Términos y Condiciones.'
      });
      return;
    }

    // 3) Validaciones normales
    if (!this.cart.snapshot.items.length) {
      await Swal.fire({
        icon: 'warning',
        title: 'Carrito vacío',
        text: 'Agregá productos para continuar.'
      });
      return;
    }

    if (!this.buyerEmail || !this.buyerName) {
      await Swal.fire({
        icon: 'info',
        title: 'Datos incompletos',
        text: 'Email y nombre son obligatorios.'
      });
      return;
    }

  // ✅ Validar formato de email
  if (!this.isValidEmail(this.buyerEmail)) {
    await Swal.fire({
      icon: 'error',
      title: 'Email inválido',
      text: 'Ingresá un email válido. Ejemplo: nombre@ejemplo.com'
    });
    return;
  }


    // 4) Validar teléfono para WhatsApp
    if (!this.isValidWhatsappPhone(this.buyerPhone)) {
      await Swal.fire({
        icon: 'error',
        title: 'Teléfono inválido',
        text: 'Ingresá un número de WhatsApp válido: solo números, sin 0 ni 15 al inicio. Ejemplo correcto: 3512345678.'
      });
      return;
    }

    



    const dto: any = {
      buyerEmail: this.buyerEmail,
      buyerName: this.buyerName,
      buyerPhone: this.buyerPhone,
      deliveryMethod: this.deliveryMethod,
      deliveryCost: 0, // siempre 0
      items: this.cart.snapshot.items.map(i => ({
        productId: i.productId,
        quantity: i.quantity
      }))
    };

    if (this.deliveryMethod === 'DELIVERY') {
      dto.address = this.address;
    }

    this.submitting = true;
    this.orders.createOrder(dto).subscribe({
      next: async (o) => {
        this.submitting = false;
        this.cart.clear();
        this.termsAccepted = false;
        const res = await Swal.fire({
          icon: 'success',
          title: '¡Pedido confirmado!',
          html: `Tu número de pedido es <b>#${o.id}</b>.`,
          confirmButtonText: 'Ir al catálogo'
        });
        if (res.isConfirmed) {
          this.router.navigate(['/catalogo']);
        }
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