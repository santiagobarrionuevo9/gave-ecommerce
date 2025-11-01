export interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  imageUrl?: string;
  sku?: string | null;
  slug?: string | null;
}

export interface CartState {
  items: CartItem[];
  itemsTotal: number;  // suma de (price * quantity)
}