export interface Createproductdto {
    typeId: number;
  name: string;
  slug: string;
  shortDesc?: string | null;
  description?: string | null;
  isActive?: boolean;
  sku: string;
  price: number;
  stock: number;
  // 👇 NUEVO
  discountThreshold?: number | null;
  discountPercent?: number | null;
  // 👇 NUEVO: info de stock que viene del backend
  availableStock?: number | null;
  stockLevel?: 'DANGER' | 'MODERATE' | 'OK' | null;
  stockLowThreshold?: number | null;
  stockMediumThreshold?: number | null;


}
