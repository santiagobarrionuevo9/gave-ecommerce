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
}
