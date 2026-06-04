import type { CreateReservationRequest, ReservationResponse } from '../types/reservation';

const RESERVATION_SERVICE_URL =
  (import.meta.env.VITE_RESERVATION_SERVICE_URL as string | undefined)?.replace(/\/$/, '') ?? '';

const BASE_URL = `${RESERVATION_SERVICE_URL}/api/v1/reservations`;

export async function createReservation(request: CreateReservationRequest): Promise<ReservationResponse> {
  const response = await fetch(BASE_URL, {
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  if (!response.ok) {
    throw new Error(`Reservation could not be created: ${response.status} ${response.statusText}`);
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (!contentType.includes('application/json')) {
    return {};
  }

  return response.json() as Promise<ReservationResponse>;
}
