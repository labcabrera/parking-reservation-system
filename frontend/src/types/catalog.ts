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
  latitude?: number;
  longitude?: number;
  dailyRate?: number;
  currency: string;
  tags: FacilityTag[];
  /** @deprecated use lowAvailability */
  lowAvailabilityWarning?: boolean;
  lowAvailability: boolean;
  availableSpots: number;
  estimatedPrice?: Money;
  totalSpots?: number;
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
  searchSessionId?: string;
  stale?: boolean;
  content: FacilityResult[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
}

export const FacilityStatus = {
  ACTIVE: 'ACTIVE',
  MAINTENANCE: 'MAINTENANCE',
  CLOSED: 'CLOSED',
} as const;

export type FacilityStatus = (typeof FacilityStatus)[keyof typeof FacilityStatus];

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface CancellationPolicy {
  freeCancelHours: number;
  penaltyCancelMinutes: number;
}

export interface ParkingPricingRule {
  externalPricingId: string;
  estimatedDailyPrice: number;
}

export interface ParkingFacility {
  id: string;
  name: string;
  city: string;
  address: string;
  location: Coordinates;
  totalSpots: number;
  tags: FacilityTag[];
  status: FacilityStatus;
  cancellationPolicy: CancellationPolicy;
  pricingRule: ParkingPricingRule;
}

export type CreateParkingFacilityRequest = Omit<ParkingFacility, 'id'>;

export interface Pagination {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PageResponse<T> {
  content: T[];
  pagination: Pagination;
}

export interface ParkingFacilityListParams {
  page?: number;
  size?: number;
  rsql?: string;
}

export interface InventorySlot {
  slotStart: string;
  capacity: number;
  reserved: number;
  free: number;
}
