import { Component, computed, OnInit, signal } from '@angular/core';
import { Producttypedto } from '../../interface/product/producttypedto';
import { Pages } from '../../interface/product/pages';
import { Productdto } from '../../interface/product/productdto';
import { Imageproductdto } from '../../interface/product/imageproductdto';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProductService, SearchParams } from '../../service/product.service';
import { debounceTime, switchMap, tap } from 'rxjs';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-productbrowse',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,RouterModule],
  templateUrl: './productbrowse.component.html',
  styleUrl: './productbrowse.component.css'
})
export class ProductbrowseComponent implements OnInit {
  types: Producttypedto[] = [];
  page!: Pages<Productdto>;
  imagesMap: Record<number, Imageproductdto | null> = {};
  loading = signal(false);

  form!: FormGroup;           // se crea en ngOnInit
  currentPage = signal(0);

  showingRange = computed(() => {
    if (!this.page) return '';
    const start = this.page.number * this.page.size + 1;
    const end = Math.min((this.page.number + 1) * this.page.size, this.page.totalElements);
    return `${start}–${end} de ${this.page.totalElements}`;
  });

  constructor(private fb: FormBuilder, private api: ProductService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      q: [''],
      typeId: [null],          // null en lugar de '' ✅
      sort: ['name,asc'],
      size: [12]
    });

    this.api.getTypes().subscribe(ts => this.types = ts);

    this.form.valueChanges.pipe(
      debounceTime(300),
      tap(() => this.currentPage.set(0)),
      switchMap(() => this.fetch())
    ).subscribe();

    this.fetch().subscribe();
  }

  fetch() {
    this.loading.set(true);
    const v = this.form.getRawValue();       // más seguro que .value

    const params: SearchParams = {
      q: v.q ?? '',
      typeId: v.typeId !== null ? Number(v.typeId) : null,
      active: true,
      page: this.currentPage(),
      size: Number(v.size ?? 12),
      sort: v.sort ?? 'name,asc'
    };

    return this.api.searchProducts(params).pipe(
      tap(page => { this.page = page; }),
      switchMap(page => this.api.loadFirstImages(page.content)),
      tap(mapImg => {
        this.imagesMap = mapImg;
        this.loading.set(false);
      })
    );
  }

  goTo(page: number) {
    if (!this.page) return;
    if (page < 0 || page >= this.page.totalPages) return;
    this.currentPage.set(page);
    this.fetch().subscribe();
  }

  getImageUrl(p: Productdto): string {
    const img = this.imagesMap[p.id];
    return img?.url || 'https://via.placeholder.com/400x300?text=Sin+imagen';
  }

  onClear() {
    this.form.patchValue({ q: '' });
  }
}
