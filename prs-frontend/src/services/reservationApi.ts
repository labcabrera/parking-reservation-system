import type { CreateReservationRequest, ReservationResponse } from '../types/reservation';
import { selectCheckoutOption } from './checkoutApi';

export async function createReservation(request: CreateReservationRequest): Promise<ReservationResponse> {
  const checkout = await selectCheckoutOption(request);

  return {
    checkIn: checkout.checkIn,
    checkOut: checkout.checkOut,
    currency: checkout.currency,
    estimatedPrice: checkout.amount,
    expiresAt: checkout.expiresAt,
    facilityId: checkout.facilityId,
    id: checkout.checkoutId,
    reservationId: checkout.checkoutId,
    status: checkout.status,
  };
}
