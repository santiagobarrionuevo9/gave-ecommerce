import { Component, OnInit, signal } from '@angular/core';
import { Productdto } from '../../interface/product/productdto';
import { Imageproductdto } from '../../interface/product/imageproductdto';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProductService } from '../../service/product.service';
import { CommonModule } from '@angular/common';
import { CartService } from '../../service/cart.service';
import { FormsModule } from '@angular/forms';

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

  constructor(private route: ActivatedRoute,
              private api: ProductService,
              private cart: CartService) {}

  addToCart() {
    const img = this.images?.[0]?.url;
    this.cart.add(this.product, this.qty, img);
    alert('Producto agregado al carrito ✅');
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
  /** Mueve la imagen clickeada al comienzo (como principal) */
  selectAsMain(img: Imageproductdto): void {
    if (!img || !this.images?.length) return;
    const idx = this.images.findIndex(i => i && i.id === img.id);
    if (idx <= 0) return; // -1 no existe, 0 ya es principal
    const [picked] = this.images.splice(idx, 1); // nunca undefined por el guard
    this.images.unshift(picked);
  }

  /** trackBy para mejorar performance y evitar warnings */
  trackByImgId = (_: number, item: Imageproductdto) => item?.id ?? _;
  mainImage(): string {
    return this.images.length ? this.images[0].url : 'https://via.placeholder.com/800x600?text=Sin+imagen';
  }
}
