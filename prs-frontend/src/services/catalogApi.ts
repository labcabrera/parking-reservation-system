import type {
  CreateParkingFacilityRequest,
  FacilityResult,
  InventorySlot,
  PageResponse,
  ParkingFacility,
  ParkingFacilityListParams,
  SearchRequest,
  SearchResponse,
} from '../types/catalog';

const BASE_URL = '/api/v1/parking-facilities';

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

async function parseJsonResponse<T>(response: Response, message: string): Promise<T> {
  const contentType = response.headers.get('content-type') ?? '';

  if (!response.ok) {
    throw new Error(`${message}: ${response.status} ${response.statusText}`);
  }

  if (!contentType.includes('application/json')) {
    throw new Error(`${message}: expected JSON response but received ${contentType || 'unknown content type'}`);
  }

  return response.json() as Promise<T>;
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
  if (params.page != null) searchParams.set('page', String(params.page));
  if (params.size != null) searchParams.set('limit', String(params.size));
  if (params.size != null) searchParams.set('size', String(params.size));

  const response = await fetch(`${BASE_URL}/availability?${searchParams.toString()}`);
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
    size: page.pagination.size,
    totalElements: page.pagination.totalElements,
    totalPages: page.pagination.totalPages,
  };
}

export async function listParkingFacilities(
  params: ParkingFacilityListParams = {},
): Promise<PageResponse<ParkingFacility>> {
  const searchParams = new URLSearchParams();
  if (params.page != null) searchParams.set('page', String(params.page));
  if (params.size != null) searchParams.set('size', String(params.size));
  if (params.rsql) searchParams.set('rsql', params.rsql);

  const query = searchParams.toString();
  const response = await fetch(query ? `${BASE_URL}?${query}` : BASE_URL);

  return parseJsonResponse<PageResponse<ParkingFacility>>(response, 'Parking facilities could not be loaded');
}

export async function getParkingFacility(facilityId: string): Promise<ParkingFacility> {
  const response = await fetch(`${BASE_URL}/${encodeURIComponent(facilityId)}`);

  return parseJsonResponse<ParkingFacility>(response, 'Parking facility could not be loaded');
}

export async function createParkingFacility(
  request: CreateParkingFacilityRequest,
): Promise<ParkingFacility> {
  const response = await fetch(BASE_URL, {
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  return parseJsonResponse<ParkingFacility>(response, 'Parking facility could not be created');
}

export async function getFacilityInventory(
  facilityId: string,
  start: string,
  end: string,
): Promise<InventorySlot[]> {
  const searchParams = new URLSearchParams({ end, start });
  const response = await fetch(`${BASE_URL}/${encodeURIComponent(facilityId)}/inventory?${searchParams.toString()}`);

  return parseJsonResponse<InventorySlot[]>(response, 'Parking inventory could not be loaded');
}
