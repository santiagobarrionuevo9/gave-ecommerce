import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Producttypedto } from '../interface/product/producttypedto';
import { forkJoin, map, Observable, of } from 'rxjs';
import { Pages } from '../interface/product/pages';
import { Productdto } from '../interface/product/productdto';
import { Imageproductdto } from '../interface/product/imageproductdto';
import { Createproductdto } from '../interface/product/createproductdto';
import { StockChangeDTO } from '../interface/product/stockchangedto';
import { environment } from '../../environments/environment.prod';
import { BulkDiscountByNameRequest } from '../interface/product/BulkDiscountByNameRequest';
import { BulkDiscountByNameResponse } from '../interface/product/BulkDiscountByNameResponse';

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

  private readonly base = environment.apiBase + '/api'; // ajustá si tenés proxy (angular.json)

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

  // src/app/service/product.service.ts
  createProduct(body: Createproductdto) {
  return this.http.post<Productdto>(`${this.base}/products`, body);
}

  addImage(productId: number, body: { url: string; altText?: string; sortOrder?: number }) {
    return this.http.post<Imageproductdto>(`${this.base}/products/${productId}/images`, body);
  }

  // src/app/service/product.service.ts
  uploadImage(productId: number, file: File, altText?: string, sortOrder?: number) {
    const form = new FormData();
    form.append('file', file);
    if (altText != null) form.append('altText', altText);
    if (sortOrder != null) form.append('sortOrder', String(sortOrder));
    return this.http.post<Imageproductdto>(`${this.base}/images/${productId}/images/upload`, form);
  }

  // src/app/service/product.service.ts
  uploadImages(productId: number, files: File[], altText?: string, sortStart?: number) {
    const form = new FormData();
    files.forEach(f => form.append('files', f));      // clave 'files' varias veces
    if (altText != null) form.append('altText', altText);
    if (sortStart != null) form.append('sortStart', String(sortStart));
    return this.http.post<Imageproductdto[]>(`${this.base}/images/${productId}/images/upload-multiple`, form);
  }

  // === NUEVOS ===
  getProductById(id: number): Observable<Productdto> {
    return this.http.get<Productdto>(`${this.base}/products/${id}`);
  }

  updateProduct(id: number, body: Createproductdto): Observable<Productdto> {
    return this.http.put<Productdto>(`${this.base}/products/${id}`, body);
  }

  // NUEVOS: editar/borrar (viven en /api/products/images/{id})
  updateImage(imageId: number, body: { altText?: string; sortOrder?: number }) {
    return this.http.put<Imageproductdto>(`${this.base}/products/images/${imageId}`, body);
  }

  deleteImage(imageId: number) {
    return this.http.delete<void>(`${this.base}/products/images/${imageId}`);
  }

  updateStock(id: number, body: StockChangeDTO): Observable<Productdto> {
    return this.http.post<Productdto>(`${this.base}/products/${id}/stock`, body);
  }

  // ---------- Categorías ----------
  createType(dto: Producttypedto): Observable<Producttypedto> {
    return this.http.post<Producttypedto>(`${this.base}/products/types`, dto);
  }
  updateType(id: number, dto: Producttypedto): Observable<Producttypedto> {
    // si no querés PUT, podés hacer POST a /types/{id}. Ajustá backend si hace falta.
    return this.http.put<Producttypedto>(`${this.base}/products/types/${id}`, dto);
  }
  deleteType(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/products/types/${id}`);
  }

  // product.service.ts
    getLowStock(): Observable<Productdto[]> {
      return this.http.get<Productdto[]>(`${this.base}/products/low-stock`);
    }

    bulkDiscountByName(payload: BulkDiscountByNameRequest): Observable<BulkDiscountByNameResponse> {
    return this.http.post<BulkDiscountByNameResponse>(
      `${this.base}/products/bulk/discount/by-name`,
      payload
    );
  }

}
