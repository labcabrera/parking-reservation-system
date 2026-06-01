import { useQuery } from '@tanstack/react-query';
import { searchParking } from '../services/catalogApi';
import type { FacilityResult, SearchRequest } from '../types/catalog';

export function useParkingSearch(params: SearchRequest | null) {
  return useQuery<FacilityResult[], Error>({
    queryKey: ['parking-search', params],
    queryFn: () => searchParking(params!),
    enabled: params !== null && params.q.length > 0,
    staleTime: 30_000,
  });
}
