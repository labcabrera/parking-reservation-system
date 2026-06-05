import { useQuery } from '@tanstack/react-query';
import { searchParking } from '../services/bffApi';
import type { FacilityResult, SearchRequest, SearchResponse } from '../types/catalog';

export interface ParkingSearchResult {
  facilities: FacilityResult[];
  searchSessionId: string | undefined;
  stale: boolean;
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
}

export function useParkingSearch(params: SearchRequest | null) {
  const query = useQuery<SearchResponse, Error>({
    queryKey: ['parking-search', params],
    queryFn: () => searchParking(params!),
    enabled: params !== null && params.q.length > 0,
    staleTime: 30_000,
  });

  const result: ParkingSearchResult = {
    facilities: query.data?.content ?? [],
    searchSessionId: query.data?.searchSessionId,
    stale: query.data?.stale ?? false,
    page: query.data?.page ?? 0,
    size: query.data?.size ?? 20,
    totalPages: query.data?.totalPages ?? 0,
    totalElements: query.data?.totalElements ?? 0,
  };

  return { ...query, result };
}
