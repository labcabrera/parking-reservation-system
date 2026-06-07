import type {
  CreateParkingFacilityRequest,
  InventorySlot,
  PageResponse,
  ParkingFacility,
  ParkingFacilityListParams,
} from '../types/catalog';
import { authenticatedFetch, getBaseUrl, parseJsonResponse } from './http';

const BASE_URL = `${getBaseUrl('VITE_PARKING_FACILITIES_SERVICE_URL', '/admin-api')}/api/v1/parking-facilities`;

export async function listParkingFacilities(
  params: ParkingFacilityListParams = {},
): Promise<PageResponse<ParkingFacility>> {
  const searchParams = new URLSearchParams();
  if (params.page != null) searchParams.set('page', String(params.page));
  if (params.size != null) searchParams.set('size', String(params.size));
  if (params.rsql) searchParams.set('rsql', params.rsql);

  const query = searchParams.toString();
  const response = await authenticatedFetch(query ? `${BASE_URL}?${query}` : BASE_URL);

  return parseJsonResponse<PageResponse<ParkingFacility>>(response, 'Parking facilities could not be loaded');
}

export async function getParkingFacility(facilityId: string): Promise<ParkingFacility> {
  const response = await authenticatedFetch(`${BASE_URL}/${encodeURIComponent(facilityId)}`);

  return parseJsonResponse<ParkingFacility>(response, 'Parking facility could not be loaded');
}

export async function createParkingFacility(
  request: CreateParkingFacilityRequest,
): Promise<ParkingFacility> {
  const response = await authenticatedFetch(BASE_URL, {
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
  const response = await authenticatedFetch(`${BASE_URL}/${encodeURIComponent(facilityId)}/inventory?${searchParams.toString()}`);

  return parseJsonResponse<InventorySlot[]>(response, 'Parking inventory could not be loaded');
}
