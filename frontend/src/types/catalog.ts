export const FacilityTag = {
  EXPRESS_ENTRY: 'EXPRESS_ENTRY',
  FREE_CANCELLATION: 'FREE_CANCELLATION',
  COVERED: 'COVERED',
  EV_CHARGING: 'EV_CHARGING',
  GUARDED: 'GUARDED',
} as const;

export type FacilityTag = (typeof FacilityTag)[keyof typeof FacilityTag];

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
  lowAvailabilityWarning: boolean;
  availableSpots: number;
}

export interface SearchRequest {
  q: string;
  checkIn: string;
  checkOut: string;
  page?: number;
  size?: number;
}
