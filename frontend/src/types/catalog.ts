export const FacilityTag = {
  EXPRESS_ENTRY: 'EXPRESS_ENTRY',
  FREE_CANCELLATION: 'FREE_CANCELLATION',
  COVERED: 'COVERED',
  EV_CHARGING: 'EV_CHARGING',
  GUARDED: 'GUARDED',
  WHEELCHAIR_ACCESSIBLE: 'WHEELCHAIR_ACCESSIBLE',
  VALET: 'VALET',
} as const;

export type FacilityTag = (typeof FacilityTag)[keyof typeof FacilityTag];

export interface Money {
  amount: number;
  currency: string;
}

export interface FacilityResult {
  facilityId: string;
  name: string;
  city: string;
  address: string;
  latitude: number;
  longitude: number;
  dailyRate: number;
  currency: string;
  tags: FacilityTag[];
  /** @deprecated use lowAvailability */
  lowAvailabilityWarning?: boolean;
  lowAvailability: boolean;
  availableSpots: number;
  estimatedPrice?: Money;
}

export interface SearchRequest {
  q: string;
  checkIn: string;
  checkOut: string;
  page?: number;
  size?: number;
  lat?: number;
  lng?: number;
  radiusKm?: number;
  features?: string[];
}

export interface SearchResponse {
  searchSessionId: string;
  stale: boolean;
  content: FacilityResult[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
}
