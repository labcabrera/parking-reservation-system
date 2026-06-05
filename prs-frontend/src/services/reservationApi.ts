import type { CreateReservationRequest, ReservationResponse } from '../types/reservation';
import { createReservation as createBffReservation } from './bffApi';

export async function createReservation(request: CreateReservationRequest): Promise<ReservationResponse> {
  return createBffReservation(request);
}
