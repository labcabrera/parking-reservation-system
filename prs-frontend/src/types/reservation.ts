export interface CreateReservationRequest {
  facilityId: string;
  checkIn: string;
  checkOut: string;
}

export interface ReservationResponse {
  reservationId?: string;
  id?: string;
  userId?: string;
  status?: string;
  facilityId?: string;
  checkIn?: string;
  checkOut?: string;
  estimatedPrice?: number;
  currency?: string;
  expiresAt?: string;
  failureReason?: string | null;
  [key: string]: unknown;
}

export interface ReservationListParams {
  start: string;
  end: string;
  page?: number;
  size?: number;
  facilityId?: string;
}
