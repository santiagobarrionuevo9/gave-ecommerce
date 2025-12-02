import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ChangeOrderStatusDTO, OrderDTO } from '../interface/order/types';
import { Observable } from 'rxjs';
import { Pages } from '../interface/product/pages';
import { OrderStatus } from '../interface/order/orderstatus';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private base = environment.apiBase; // ajustá si tenés proxy (angular.json)

  constructor(private http: HttpClient) {}

  createOrder(body: any): Observable<OrderDTO> {
    return this.http.post<OrderDTO>(`${this.base}/orders`, body);
  }

  myOrders(buyerEmail: string, page = 0, size = 10): Observable<Pages<OrderDTO>> {
    const params = new HttpParams()
      .set('buyerEmail', buyerEmail)
      .set('page', page)
      .set('size', size);
    return this.http.get<Pages<OrderDTO>>(`${this.base}/orders/mine`, { params });
  }

  /** ✅ ADMIN: listar por estado (usar /api/orders/admin) */
  listByStatus(status: OrderStatus, page = 0, size = 10): Observable<Pages<OrderDTO>> {
    const params = new HttpParams()
      .set('status', status)
      .set('page', page)
      .set('size', size);
    return this.http.get<Pages<OrderDTO>>(`${this.base}/orders/admin`, { params });
  }

  /** ✅ ADMIN: cambiar estado (usar /api/orders/admin/{id}/status) */
  changeStatus(id: number, body: ChangeOrderStatusDTO): Observable<OrderDTO> {
    return this.http.put<OrderDTO>(`${this.base}/orders/admin/${id}/status`, body);
  }

  /** Detalle pedido */
  getById(id: number): Observable<OrderDTO> {
    return this.http.get<OrderDTO>(`${this.base}/orders/${id}`);
  }
}
