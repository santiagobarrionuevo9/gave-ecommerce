import { Component, OnInit, signal } from '@angular/core';
import { OrderStatus } from '../../interface/order/orderstatus';
import { OrderDTO } from '../../interface/order/types';
import { OrderService } from '../../service/order.service';
import { nextStatusOptions, statusBadgeClass } from '../../app.config';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-adminorderstatus',
  standalone: true,
  imports: [CommonModule,FormsModule,RouterModule],
  templateUrl: './adminorderstatus.component.html',
  styleUrl: './adminorderstatus.component.css'
})
export class AdminorderstatusComponent implements OnInit {
  OrderStatus = OrderStatus;              // para el template
  statusList = Object.values(OrderStatus);

  loading = signal(false);
  error: string | null = null;

  // filtros & paginación
  currentStatus = signal<OrderStatus>(OrderStatus.PENDING);
  page = signal(0);
  size = signal(10);

  // datos
  orders: OrderDTO[] = [];
  totalPages = 0;
  totalElements = 0;

  // estado elegido por fila (select)
  nextMap = new Map<number, OrderStatus>();

  constructor(private api: OrderService) {}

  ngOnInit(): void { this.fetch(); }

  fetch(): void {
    this.loading.set(true);
    this.error = null;

    this.api.listByStatus(this.currentStatus(), this.page(), this.size())
      .subscribe({
        next: res => {
          this.orders = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          // preselecciono el primer siguiente permitido
          this.orders.forEach(o => {
            const opts = nextStatusOptions(o.status);
            if (opts.length) this.nextMap.set(o.id, opts[0]);
          });
          this.loading.set(false);
        },
        error: () => {
          this.error = 'No se pudieron cargar los pedidos.';
          this.loading.set(false);
        }
      });
  }

  badgeCls(s: OrderStatus) { return statusBadgeClass(s); }
  optionsFor(o: OrderDTO) { return nextStatusOptions(o.status); }

  changeStatus(o: OrderDTO): void {
    const ns = this.nextMap.get(o.id);
    if (!ns || ns === o.status) return;
    this.loading.set(true);
    this.api.changeStatus(o.id, { status: ns }).subscribe({
      next: updated => {
        // reemplazo en lista
        const idx = this.orders.findIndex(x => x.id === o.id);
        if (idx >= 0) this.orders[idx] = updated;
        // refresco opciones siguientes
        const opts = this.optionsFor(updated);
        if (opts.length) this.nextMap.set(updated.id, opts[0]);
        else this.nextMap.delete(updated.id);
        this.loading.set(false);
      },
      error: () => {
        this.error = 'No se pudo actualizar el estado.';
        this.loading.set(false);
      }
    });
  }

  setStatusFilter(s: OrderStatus) {
    this.currentStatus.set(s);
    this.page.set(0);
    this.fetch();
  }

  goTo(p: number) {
    if (p < 0 || p >= this.totalPages) return;
    this.page.set(p);
    this.fetch();
  }

  // 📲 WhatsApp
  buildWhatsAppLink(phone: string): string {
    if (!phone) return '#';

    let digits = phone.replace(/\D/g, '');
    if (digits.startsWith('0')) {
      digits = digits.substring(1);
    }
    if (!digits.startsWith('54')) {
      digits = '54' + digits;
    }
    return `https://wa.me/${digits}`;
  }

  buildGmailLink(email: string, orderId?: number, buyerName?: string): string {
  if (!email) return '#';

  const to = encodeURIComponent(email);
  const subject = encodeURIComponent(
    orderId ? `Consulta por pedido #${orderId}` : 'Consulta por pedido'
  );

  const bodyText = `Hola${buyerName ? ' ' + buyerName : ''},\n\n` +
                   `Te escribo por tu pedido${orderId ? ' #' + orderId : ''} en la tienda.\n\n` +
                   `¡Gracias!`;
  const body = encodeURIComponent(bodyText);

  return `https://mail.google.com/mail/?view=cm&fs=1&to=${to}&su=${subject}&body=${body}`;
}


}