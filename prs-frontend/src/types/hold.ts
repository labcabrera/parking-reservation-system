export type HoldStatus =
  | 'PENDING_PRICE'
  | 'ACTIVE'
  | 'EXPIRED'
  | 'RELEASED'
  | 'FAILED'
  | 'CONVERTED';

export interface CreateHoldRequest {
  searchSessionId: string;
  spotId: string;
  facilityId: string;
  checkIn: string;   // ISO-8601
  checkOut: string;  // ISO-8601
  estimatedPriceAmount: number;
  currency: string;
}

export interface HoldCreatedResponse {
  holdId: string;
  expiresAt: string; // ISO-8601
}

export interface Money {
  amount: number;
  currency: string;
}

export interface HoldResponse {
  holdId: string;
  searchSessionId: string;
  spotId: string;
  facilityId: string;
  status: HoldStatus;
  estimatedPrice: Money;
  confirmedPrice: Money | null;
  checkIn: string;
  checkOut: string;
  expiresAt: string;
}
