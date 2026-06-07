import type { FacilityResult, InventoryBlockType, PageResponse, ParkingCapacity, SearchRequest, SearchResponse } from '../types/catalog';
import { getBookingSessionId } from './bookingSession';
import { authenticatedFetch, getBaseUrl, parseJsonResponse } from './http';

const BFF_URL = getBaseUrl('VITE_BFF_URL');
const CHECKOUT_URL = `${BFF_URL}/api/v1/checkout`;

interface ParkingOptionDto {
  facilityId: string;
  name: string;
  city: string;
  address: string;
  capacity?: ParkingCapacity;
  totalSpots?: number;
  blockType?: InventoryBlockType;
  availableSpots: number;
  lowAvailability: boolean;
  estimatedPrice: number;
  currency: string;
}

function toFacilityResult(dto: ParkingOptionDto): FacilityResult {
  return {
    address: dto.address,
    availableSpots: dto.availableSpots,
    city: dto.city,
    currency: dto.currency,
    blockType: dto.blockType,
    capacity: dto.capacity,
    estimatedPrice: {
      amount: dto.estimatedPrice,
      currency: dto.currency,
    },
    facilityId: dto.facilityId,
    lowAvailability: dto.lowAvailability,
    name: dto.name,
    tags: [],
    totalSpots: dto.capacity?.total ?? dto.totalSpots,
  };
}

export async function searchParking(params: SearchRequest): Promise<SearchResponse> {
  const searchParams = new URLSearchParams();
  searchParams.set('q', params.q);
  searchParams.set('checkIn', params.checkIn);
  searchParams.set('checkOut', params.checkOut);
  if (params.size != null) searchParams.set('limit', String(params.size));

  const response = await authenticatedFetch(`${CHECKOUT_URL}/search?${searchParams.toString()}`);
  const payload = await parseJsonResponse<ParkingOptionDto[] | PageResponse<ParkingOptionDto>>(
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
    searchSessionId: getBookingSessionId(),
    size: page.pagination.size,
    stale: false,
    totalElements: page.pagination.totalElements,
    totalPages: page.pagination.totalPages,
  };
}
