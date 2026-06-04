export interface CreateReservationRequest {
  facilityId: string;
  checkIn: string;
  checkOut: string;
}

export interface ReservationResponse {
  reservationId?: string;
  id?: string;
  status?: string;
  facilityId?: string;
  checkIn?: string;
  checkOut?: string;
  [key: string]: unknown;
}
