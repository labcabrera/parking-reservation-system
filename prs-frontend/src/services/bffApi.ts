import type { FacilityResult, PageResponse, SearchRequest, SearchResponse } from '../types/catalog';
import type { CreateReservationRequest, ReservationResponse } from '../types/reservation';
import { getBaseUrl, parseJsonResponse, parseOptionalJsonResponse } from './http';

const BFF_URL = getBaseUrl('VITE_BFF_URL');
const FACILITIES_URL = `${BFF_URL}/api/v1/parking-facilities`;
const RESERVATIONS_URL = `${BFF_URL}/api/v1/reservations`;

interface FacilityAvailabilityDto {
  id: string;
  name: string;
  city: string;
  address: string;
  totalSpots: number;
  availableSpots: number;
  lowAvailability: boolean;
  estimatedPrice: number;
  currency: string;
}

function toFacilityResult(dto: FacilityAvailabilityDto): FacilityResult {
  return {
    address: dto.address,
    availableSpots: dto.availableSpots,
    city: dto.city,
    currency: dto.currency,
    estimatedPrice: {
      amount: dto.estimatedPrice,
      currency: dto.currency,
    },
    facilityId: dto.id,
    lowAvailability: dto.lowAvailability,
    name: dto.name,
    tags: [],
    totalSpots: dto.totalSpots,
  };
}

export async function searchParking(params: SearchRequest): Promise<SearchResponse> {
  const searchParams = new URLSearchParams();
  searchParams.set('q', params.q);
  searchParams.set('checkIn', params.checkIn);
  searchParams.set('checkOut', params.checkOut);
  if (params.size != null) searchParams.set('limit', String(params.size));

  const response = await fetch(`${FACILITIES_URL}/availability?${searchParams.toString()}`);
  const payload = await parseJsonResponse<FacilityAvailabilityDto[] | PageResponse<FacilityAvailabilityDto>>(
    response,
    'Search failed',
  );
  const page = Array.isArray(payload)
    ? {
        content: payload,
        pagination: {
          page: params.page ?? 0,
          size: payload.length,
          totalElements: payload.length,
          totalPages: payload.length > 0 ? 1 : 0,
        },
      }
    : payload;

  return {
    content: page.content.map(toFacilityResult),
    page: page.pagination.page,
    searchSessionId: crypto.randomUUID(),
    size: page.pagination.size,
    stale: false,
    totalElements: page.pagination.totalElements,
    totalPages: page.pagination.totalPages,
  };
}

export async function createReservation(request: CreateReservationRequest): Promise<ReservationResponse> {
  const response = await fetch(RESERVATIONS_URL, {
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  return parseOptionalJsonResponse<ReservationResponse>(response, 'Reservation could not be created');
}
