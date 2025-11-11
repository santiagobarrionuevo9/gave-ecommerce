export interface StockChangeDTO {
  operation: 'SET'|'INCREMENT'|'DECREMENT';
  amount: number;
  reason?: string;
}
