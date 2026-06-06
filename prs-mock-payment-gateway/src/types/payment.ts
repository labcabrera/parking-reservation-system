export interface PaymentSession {
  attemptId?: string;
  orderId?: string;
  amount?: number;
  currency?: string;
}

export interface PaymentAttemptRequest {
  orderId: string;
  amount: number;
  currency: string;
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
