import { Component, OnInit, signal } from '@angular/core';
import { Productdto } from '../../interface/product/productdto';
import { Imageproductdto } from '../../interface/product/imageproductdto';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductService } from '../../service/product.service';
import { CommonModule } from '@angular/common';
import { CartService } from '../../service/cart.service';
import { FormsModule } from '@angular/forms';

type ToastKind = 'success' | 'danger';

@Component({
  selector: 'app-detailproduct',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './detailproduct.component.html',
  styleUrl: './detailproduct.component.css'
})
export class DetailproductComponent implements OnInit {
  loading = signal(true);
  error: string | null = null;

  product!: Productdto;
  images: Imageproductdto[] = [];

  qty = 1;
  private readonly DEFAULT_DISCOUNT_THRESHOLD = 10;
  private readonly DEFAULT_DISCOUNT_PERCENT = 10;

  // estado del toast
  toast = signal<{ type: ToastKind; text: string } | null>(null);
  private toastTimer: any;

  constructor(
    private route: ActivatedRoute,
    private api: ProductService,
    private cart: CartService
  ) {}

  /** Mostrar toast 3s */
  private showToast(type: ToastKind, text: string) {
    this.toast.set({ type, text });
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 3000);
  }
  clearToast() {
    clearTimeout(this.toastTimer);
    this.toast.set(null);
  }

  addToCart() {
    try {
      if (!this.product) return;

      const q = Math.max(1, Math.floor(this.qty || 1));
      if (this.product.stock === 0) {
        this.showToast('danger', 'No hay stock disponible.');
        return;
      }
      if (q > this.product.stock) {
        this.showToast('danger', `Solo hay ${this.product.stock} en stock.`);
        return;
      }

      const img = this.images?.[0]?.url;
      this.cart.add(this.product, q, img);
      this.qty = 1; // opcional: reset cantidad
      this.showToast('success', 'Producto agregado al carrito ✅');
    } catch (e: any) {
      const msg = e?.message || 'No se pudo agregar al carrito.';
      this.showToast('danger', msg);
    }
  }

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug')!;
    this.api.getProductBySlug(slug).subscribe({
      next: (p) => {
        this.product = p;
        this.api.getImagesByProductId(p.id).subscribe({
          next: (imgs) => { this.images = imgs ?? []; this.loading.set(false); },
          error: () => { this.images = []; this.loading.set(false); }
        });
      },
      error: () => {
        this.error = 'Producto no encontrado o inactivo.';
        this.loading.set(false);
      }
    });
  }

  /** Galería */
  selectAsMain(img: Imageproductdto): void {
    if (!img || !this.images?.length) return;
    const idx = this.images.findIndex(i => i && i.id === img.id);
    if (idx <= 0) return;
    const [picked] = this.images.splice(idx, 1);
    this.images.unshift(picked);
  }

  trackByImgId = (_: number, item: Imageproductdto) => item?.id ?? _;
  mainImage(): string {
    return this.images.length ? this.images[0].url : 'https://via.placeholder.com/800x600?text=Sin+imagen';
  }
  /** Cantidad mínima efectiva (si no viene del backend, usamos default 10) */
  get discountThreshold(): number {
    if (!this.product) return this.DEFAULT_DISCOUNT_THRESHOLD;
    return this.product.discountThreshold ?? this.DEFAULT_DISCOUNT_THRESHOLD;
  }

  /** % de descuento efectivo (si no viene del backend, usamos default 10) */
  get discountPercent(): number {
    if (!this.product) return this.DEFAULT_DISCOUNT_PERCENT;
    return this.product.discountPercent ?? this.DEFAULT_DISCOUNT_PERCENT;
  }

  /** ¿Hay descuento real (> 0 %)? */
  get hasDiscount(): boolean {
    if (!this.product) return false;

    // Si el back deja percent en 0, no mostramos promo
    const percent = this.product.discountPercent ?? this.DEFAULT_DISCOUNT_PERCENT;
    return percent > 0;
  }
}
