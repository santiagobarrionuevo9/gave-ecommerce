export interface BulkPriceIncreaseByNameRequest {
  keyword: string;        // marca / texto a buscar en name
  percent: number;        // 10 => +10%
  activeOnly?: boolean;   // default true
}
