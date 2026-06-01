import type { FacilityResult, SearchRequest } from '../types/catalog';

const BASE_URL = '/api/v1/catalog';

export async function searchParking(params: SearchRequest): Promise<FacilityResult[]> {
  const searchParams = new URLSearchParams();
  searchParams.set('q', params.q);
  searchParams.set('checkIn', params.checkIn);
  searchParams.set('checkOut', params.checkOut);
  if (params.page != null) searchParams.set('page', String(params.page));
  if (params.size != null) searchParams.set('size', String(params.size));

  const response = await fetch(`${BASE_URL}/search?${searchParams.toString()}`);

  if (!response.ok) {
    throw new Error(`Search failed: ${response.status} ${response.statusText}`);
  }

  const data = await response.json();
  // Spring Data Page response wraps results in `content`
  return Array.isArray(data) ? data : (data.content ?? []);
}
