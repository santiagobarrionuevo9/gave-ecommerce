import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Producttypedto } from '../interface/product/producttypedto';
import { forkJoin, map, Observable, of } from 'rxjs';
import { Pages } from '../interface/product/pages';
import { Productdto } from '../interface/product/productdto';
import { Imageproductdto } from '../interface/product/imageproductdto';

export interface SearchParams {
  q?: string | null;
  typeId?: number | null;
  active?: boolean | null;
  page?: number;     // 0-based
  size?: number;     // e.g. 12
  sort?: string;     // "price,asc" | "name,asc" | "name,desc" | ...
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly base = 'http://localhost:9011/api'; // ajustá si tenés proxy (angular.json)

  constructor(private http: HttpClient) {}

  getTypes(): Observable<Producttypedto[]> {
    return this.http.get<Producttypedto[]>(`${this.base}/products/types`);
  }

  searchProducts(params: SearchParams): Observable<Pages<Productdto>> {
    let p = new HttpParams();
    if (params.q)      p = p.set('q', params.q);
    if (params.typeId !== null && params.typeId !== undefined) p = p.set('typeId', params.typeId);
    if (params.active !== null && params.active !== undefined) p = p.set('active', params.active);
    p = p.set('page', String(params.page ?? 0));
    p = p.set('size', String(params.size ?? 12));
    p = p.set('sort', params.sort ?? 'name,asc');

    return this.http.get<Pages<Productdto>>(`${this.base}/products`, { params: p });
  }

  getImages(productId: number): Observable<Imageproductdto[]> {
    return this.http.get<Imageproductdto[]>(`${this.base}/products/${productId}/images`);
  }

  /**
   * Devuelve un mapa productId -> primera imagen (o null si no hay)
   */
  loadFirstImages(products: Productdto[]): Observable<Record<number, Imageproductdto | null>> {
    if (!products?.length) return of({});
    const requests = products.map(p => this.getImages(p.id));
    return forkJoin(requests).pipe(
      map(results => {
        const mapObj: Record<number, Imageproductdto | null> = {};
        products.forEach((p, i) => {
          const imgs = results[i] ?? [];
          mapObj[p.id] = imgs.length ? imgs[0] : null;
        });
        return mapObj;
      })
    );
  }

  // src/app/service/product.service.ts
  getProductBySlug(slug: string) {
    // backend público sugerido: /api/public/products/{slug}
    return this.http.get<Productdto>(`${this.base}/public/products/${slug}`);
  }

  getImagesByProductId(productId: number) {
    return this.http.get<Imageproductdto[]>(`${this.base}/products/${productId}/images`);
  }

}
