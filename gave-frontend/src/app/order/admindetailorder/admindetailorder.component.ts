import { Component, OnInit, signal } from '@angular/core';
import { OrderDTO } from '../../interface/order/types';
import { OrderStatus } from '../../interface/order/orderstatus';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { OrderService } from '../../service/order.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { nextStatusOptions, statusBadgeClass } from '../../app.config';

@Component({
  selector: 'app-admindetailorder',
  standalone: true,
  imports: [CommonModule,FormsModule,RouterModule],
  templateUrl: './admindetailorder.component.html',
  styleUrl: './admindetailorder.component.css'
})
export class AdmindetailorderComponent implements OnInit {
  loading = signal(true);
  error: string | null = null;

  order!: OrderDTO;
  nextStatus?: OrderStatus;

  constructor(private route: ActivatedRoute, private api: OrderService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getById(id).subscribe({
      next: (o) => {
        this.order = o;
        const opts = nextStatusOptions(o.status);
        this.nextStatus = opts[0];
        this.loading.set(false);
      },
      error: () => { this.error = 'No se pudo cargar el pedido.'; this.loading.set(false); }
    });
  }

  badgeCls(s: OrderStatus) { return statusBadgeClass(s); }
  options() { return nextStatusOptions(this.order.status); }

  update(): void {
    if (!this.nextStatus || this.nextStatus === this.order.status) return;
    this.loading.set(true);
    this.api.changeStatus(this.order.id, { status: this.nextStatus }).subscribe({
      next: (upd) => { this.order = upd; const opts = this.options(); this.nextStatus = opts[0]; this.loading.set(false); },
      error: () => { this.error = 'No se pudo actualizar el estado.'; this.loading.set(false); }
    });
  }
}