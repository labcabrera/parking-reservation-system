import type { SearchRequest, SearchResponse } from '../types/catalog';

const BASE_URL = '/api/v1/catalog';

export async function searchParking(params: SearchRequest): Promise<SearchResponse> {
  const searchParams = new URLSearchParams();
  searchParams.set('q', params.q);
  searchParams.set('checkIn', params.checkIn);
  searchParams.set('checkOut', params.checkOut);
  if (params.page != null) searchParams.set('page', String(params.page));
  if (params.size != null) searchParams.set('size', String(params.size));
  if (params.lat != null) searchParams.set('lat', String(params.lat));
  if (params.lng != null) searchParams.set('lng', String(params.lng));
  if (params.radiusKm != null) searchParams.set('radiusKm', String(params.radiusKm));
  if (params.features && params.features.length > 0) {
    params.features.forEach((f) => searchParams.append('features', f));
  }

  const response = await fetch(`${BASE_URL}/search?${searchParams.toString()}`);

  if (!response.ok) {
    throw new Error(`Search failed: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<SearchResponse>;
}

export function subscribeToAvailability(
  facilityId: string,
  onUpdate: (data: unknown) => void,
): EventSource {
  const es = new EventSource(`${BASE_URL}/availability/stream?facilityId=${encodeURIComponent(facilityId)}`);
  es.addEventListener('availability-update', (event: MessageEvent) => {
    try {
      onUpdate(JSON.parse(event.data));
    } catch {
      // ignore parse errors
    }
  });
  return es;
}
