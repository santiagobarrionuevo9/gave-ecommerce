import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor';
import { OrderStatus } from './interface/order/orderstatus';

export const appConfig: ApplicationConfig = {
  providers: [provideZoneChangeDetection({ eventCoalescing: true }), provideRouter(routes),provideHttpClient(withInterceptors([authInterceptor])),]
};

export function statusBadgeClass(s: OrderStatus): string {
  switch (s) {
    case OrderStatus.PENDING:          return 'badge text-bg-secondary';
    case OrderStatus.ACCEPTED:         return 'badge text-bg-info';
    case OrderStatus.PREPARING:        return 'badge text-bg-primary';
    case OrderStatus.OUT_FOR_DELIVERY: return 'badge text-bg-warning';
    case OrderStatus.DELIVERED:        return 'badge text-bg-success';
    case OrderStatus.CANCELED:         return 'badge text-bg-danger';
    default:                           return 'badge text-bg-light';
  }
}

/** Opciones “sanas” de siguiente estado, según el actual */
export function nextStatusOptions(current: OrderStatus): OrderStatus[] {
  switch (current) {
    case OrderStatus.PENDING:
      return [OrderStatus.ACCEPTED, OrderStatus.CANCELED];
    case OrderStatus.ACCEPTED:
      return [OrderStatus.PREPARING, OrderStatus.CANCELED];
    case OrderStatus.PREPARING:
      return [OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELED];
    case OrderStatus.OUT_FOR_DELIVERY:
      return [OrderStatus.DELIVERED, OrderStatus.CANCELED];
    case OrderStatus.DELIVERED:
    case OrderStatus.CANCELED:
      return []; // finales
    default:
      return [];
  }
}
