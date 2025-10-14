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
}
