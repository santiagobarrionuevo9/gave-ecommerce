import { Injectable } from '@angular/core';
import { CartState } from '../interface/order/cartitem';
import { BehaviorSubject } from 'rxjs';
import { Productdto } from '../interface/product/productdto';

const KEY = 'gave.cart.v1';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private state: CartState = this.load();
  private subject = new BehaviorSubject<CartState>(this.recalc(this.state));
  state$ = this.subject.asObservable();

  private load(): CartState {
    try { return JSON.parse(localStorage.getItem(KEY) || '{"items":[], "itemsTotal":0}'); }
    catch { return { items: [], itemsTotal: 0 }; }
  }
  private save() { localStorage.setItem(KEY, JSON.stringify(this.state)); }

  private recalc(s: CartState): CartState {
    s.itemsTotal = s.items.reduce((acc, it) => acc + it.price * it.quantity, 0);
    return s;
  }

  get snapshot(): CartState { return this.subject.value; }

  add(p: Productdto, qty = 1, imageUrl?: string) {
    const it = this.state.items.find(i => i.productId === p.id);
    if (it) it.quantity += qty; else {
      this.state.items.push({
        productId: p.id, name: p.name, price: Number(p.price),
        imageUrl, quantity: qty, sku: p.sku, slug: p.slug
      });
    }
    this.emit();
  }

  setQty(productId: number, qty: number) {
    const it = this.state.items.find(i => i.productId === productId);
    if (!it) return;
    it.quantity = Math.max(1, qty);
    this.emit();
  }

  remove(productId: number) {
    this.state.items = this.state.items.filter(i => i.productId !== productId);
    this.emit();
  }

  clear() { this.state.items = []; this.emit(); }

  private emit() {
    this.recalc(this.state); this.save(); this.subject.next({ ...this.state, items:[...this.state.items] });
  }
}
