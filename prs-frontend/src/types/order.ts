import type { Money, PageResponse } from './catalog';

export type OrderStatus = 'CREATED' | 'PAYMENT_IN_PROGRESS' | 'PAID' | 'PAYMENT_FAILED' | 'EXPIRED';

export interface Order {
  id: string;
  holdId: string;
  expiresAt: string;
  amount: number;
  currency: string;
  status: OrderStatus;
  createdAt?: string | null;
  lastPaymentAttemptId?: string | null;
  failureReason?: string | null;
}

export interface CreateOrderRequest {
  holdId: string;
  expiresAt: string;
  amount: Money['amount'];
  currency: Money['currency'];
}

export type OrderPage = PageResponse<Order>;
