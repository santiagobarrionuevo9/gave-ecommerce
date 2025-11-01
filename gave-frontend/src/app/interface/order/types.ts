import { DeliveryMethod, OrderStatus } from "./orderstatus";


export interface CreateOrderItemDTO {
  productId: number;
  quantity: number;  // >= 1
}

export interface ShippingAddressDTO {
  street: string;
  number?: string | null;
  apt?: string | null;
  reference?: string | null;
  city: string;
  province: string;
  postalCode?: string | null;
  lat?: number | null;
  lng?: number | null;
}

export interface CreateOrderDTO {
  buyerEmail: string;
  buyerName: string;
  buyerPhone?: string | null;

  deliveryMethod: DeliveryMethod;         // DELIVERY o PICKUP
  address?: ShippingAddressDTO | null;    // requerido si DELIVERY
  deliveryCost?: number | null;           // 0 si PICKUP

  items: CreateOrderItemDTO[];
}

export interface OrderItemDTO {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface OrderDTO {
  id: number;
  buyerEmail: string;
  buyerName: string;
  buyerPhone?: string | null;

  status: OrderStatus;

  deliveryMethod: DeliveryMethod;
  address?: ShippingAddressDTO | null;

  itemsTotal: number;
  grandTotal: number;     // itemsTotal + deliveryCost
  deliveryCost?: number | null;

  createdAt: string;      // ISO
  updatedAt: string;      // ISO

  items: OrderItemDTO[];
}

export interface ChangeOrderStatusDTO {
  status: OrderStatus;
}
