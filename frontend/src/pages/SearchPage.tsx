import { useState } from 'react';
import { Alert, Stack } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { ParkingResultsSection } from '../components/results/ParkingResultsSection';
import { ParkingSearchForm } from '../components/search/ParkingSearchForm';
import { SearchHero } from '../components/search/SearchHero';
import { useParkingSearch } from '../hooks/useParkingSearch';
import { useParkingSelection } from '../hooks/useParkingSelection';
import type { SearchRequest } from '../types/catalog';

export default function SearchPage() {
  const { t } = useTranslation();
  const [location, setLocation] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [submittedParams, setSubmittedParams] = useState<SearchRequest | null>(null);

  const { isLoading, isError, error, result } = useParkingSearch(submittedParams);
  const { reservationTimeLeft, selectedFacilityId, selectFacility } = useParkingSelection(result.facilities);

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    if (!location.trim() || !checkIn || !checkOut) return;

    setSubmittedParams({ q: location.trim(), checkIn, checkOut });
  }

  return (
    <Stack spacing={4}>
      <SearchHero>
        <ParkingSearchForm
          checkIn={checkIn}
          checkOut={checkOut}
          isLoading={isLoading}
          location={location}
          onCheckInChange={setCheckIn}
          onCheckOutChange={setCheckOut}
          onLocationChange={setLocation}
          onSubmit={handleSubmit}
        />
      </SearchHero>

      {isError && (
        <Alert severity="error">{error?.message ?? t('search.error')}</Alert>
      )}

      {result.facilities.length === 0 && !isLoading && submittedParams && (
        <Alert severity="info">{t('search.empty')}</Alert>
      )}

      {result.facilities.length > 0 && (
        <ParkingResultsSection
          facilities={result.facilities}
          location={submittedParams?.q ?? location}
          onSelectFacility={selectFacility}
          reservationTimeLeft={reservationTimeLeft}
          selectedFacilityId={selectedFacilityId}
        />
      )}
    </Stack>
  );
}
