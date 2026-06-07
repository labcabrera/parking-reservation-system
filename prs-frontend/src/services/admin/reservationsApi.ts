import type { PageResponse } from '../../types/catalog';
import type { ReservationListParams, ReservationResponse } from '../../types/reservation';
import { authenticatedFetch, getBaseUrl, parseJsonResponse } from '../http';

const BASE_URL = `${getBaseUrl('VITE_PARKING_FACILITIES_SERVICE_URL', '/admin-api')}/api/v1/reservations`;

export async function listReservations(params: ReservationListParams): Promise<PageResponse<ReservationResponse>> {
  const searchParams = new URLSearchParams({
    end: params.end,
    page: String(params.page ?? 0),
    size: String(params.size ?? 20),
    start: params.start,
  });
  if (params.facilityId) searchParams.set('facilityId', params.facilityId);

  const response = await authenticatedFetch(`${BASE_URL}?${searchParams.toString()}`);
  return parseJsonResponse<PageResponse<ReservationResponse>>(response, 'Reservations could not be loaded');
}
