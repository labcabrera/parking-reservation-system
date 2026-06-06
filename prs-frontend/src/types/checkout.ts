export interface Checkout {
  checkoutId: string;
  status?: string;
  facilityId?: string;
  facilityName?: string | null;
  checkIn?: string;
  checkOut?: string;
  expiresAt?: string;
  amount?: number;
  currency?: string;
  paymentStatus?: string | null;
  redirectUrl?: string | null;
}

export interface SelectOptionRequest {
  facilityId: string;
  checkIn: string;
  checkOut: string;
}

export interface PaymentMethod {
  id?: string;
  code: string;
  displayName: string;
  type?: string;
  status?: string;
  gatewayProvider?: string;
  iconUrl?: string | null;
  displayOrder?: number | null;
}

export interface PaymentAttemptResult {
  attemptId: string;
  status: string;
  redirectUrl?: string | null;
}
