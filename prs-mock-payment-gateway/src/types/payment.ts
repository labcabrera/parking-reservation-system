export interface PaymentSession {
  attemptId?: string;
  orderId?: string;
  amount?: number;
  currency?: string;
}

export interface PaymentAttemptRequest {
  paymentAttemptId?: string;
  orderId: string;
  idempotencyKey?: string;
  paymentMethodCode?: string;
  amount: number;
  currency: string;
  ecommerceCallbackUrl?: string;
  callbackUrl: string;
}

export interface PaymentAttemptResponse {
  attemptId: string;
  status: 'SUCCESS' | 'FAILED' | string;
  redirectUrl: string;
}

export interface CardFormState {
  cardholderName: string;
  cardNumber: string;
  expiry: string;
  cvc: string;
}
