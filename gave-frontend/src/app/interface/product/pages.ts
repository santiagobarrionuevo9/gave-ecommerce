export interface Pages <T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;      // página actual (0-based)
  size: number;        // tamaño de página
  first: boolean;
  last: boolean;
}
