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
  const DEFAULT_QTY = 10;
  const DEFAULT_PCT = 10;

  let total = 0;

  for (const it of s.items) {
    const qty = it.quantity;
    const unit = it.price;
    const gross = unit * qty; // subtotal original

    // valores efectivos
    const threshold = it.discountThreshold ?? DEFAULT_QTY;
    const percent   = it.discountPercent ?? DEFAULT_PCT;

    let discount = 0;

    // si percent = 0 => no hay descuento
    if (qty >= threshold && percent > 0) {
      discount = Number((gross * (percent / 100)).toFixed(2));
    }

    const net = gross - discount;

    it.discountAmount = discount;
    it.lineTotal = net;

    total += net;
  }

  s.itemsTotal = total;
  return s;
}


  get snapshot(): CartState { return this.subject.value; }

  add(p: Productdto, qty = 1, imageUrl?: string) {
  const it = this.state.items.find(i => i.productId === p.id);
  if (it) {
    it.quantity += qty;
  } else {
    this.state.items.push({
      productId: p.id,
      name: p.name,
      price: Number(p.price),
      imageUrl,
      quantity: qty,
      sku: p.sku,
      slug: p.slug,

      // 👇 guardamos los datos de descuento del producto
      discountThreshold: p.discountThreshold ?? null,
      discountPercent: p.discountPercent ?? null
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
