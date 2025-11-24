export interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  imageUrl?: string;
  sku?: string | null;
  slug?: string | null;
// 👇 NUEVO: info de descuento (opcional)
  discountThreshold?: number | null;
  discountPercent?: number | null;

  // 👇 NUEVO: para dejar guardado lo calculado
  lineTotal?: number;      // total con descuento
  discountAmount?: number; // descuento aplicado en ese ítem
}

export interface CartState {
  items: CartItem[];
  itemsTotal: number;  // suma de (price * quantity)
}