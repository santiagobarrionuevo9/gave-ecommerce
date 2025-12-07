// src/app/interface/product/productdto.ts
export interface Productdto {
  id: number;
  typeId: number;
  name: string;
  slug: string;
  shortDesc?: string | null;
  description?: string | null;
  isActive?: boolean | null;
  sku: string;
  price: number;
  stock: number;
  createdAt: string; // ISO de backend 👈

  // 👇 Descuento
  discountThreshold?: number | null;
  discountPercent?: number | null;

  // 👇 NUEVO: info de stock que viene del backend
  availableStock?: number | null;
  stockLevel?: 'DANGER' | 'MODERATE' | 'OK' | null;
  
  stockLowThreshold?: number | null;
  stockMediumThreshold?: number | null;

}
