export interface BulkDiscountByNameRequest {
  keyword: string;
  discountThreshold?: number | null;
  discountPercent?: number | null;
  activeOnly?: boolean;
}