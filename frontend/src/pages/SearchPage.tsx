import { useState } from 'react';
import { Alert, Stack } from '@mui/material';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { ParkingResultsSection } from '../components/results/ParkingResultsSection';
import { ReservationCheckoutView } from '../components/reservation/ReservationCheckoutView';
import { ParkingSearchForm } from '../components/search/ParkingSearchForm';
import { SearchHero } from '../components/search/SearchHero';
import { useParkingSearch } from '../hooks/useParkingSearch';
import { useParkingSelection } from '../hooks/useParkingSelection';
import { createReservation } from '../services/reservationApi';
import type { SearchRequest } from '../types/catalog';
import type { CreateReservationRequest } from '../types/reservation';

export default function SearchPage() {
  const { t } = useTranslation();
  const [location, setLocation] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [submittedParams, setSubmittedParams] = useState<SearchRequest | null>(null);

  const { isLoading, isError, error, result } = useParkingSearch(submittedParams);
  const { reservationTimeLeft, selectedFacilityId, selectFacility } = useParkingSelection(result.facilities);
  const selectedFacility = result.facilities.find((facility) => facility.facilityId === selectedFacilityId) ?? null;
  const createReservationMutation = useMutation({
    mutationFn: (request: CreateReservationRequest) => createReservation(request),
  });

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    if (!location.trim() || !checkIn || !checkOut) return;

    createReservationMutation.reset();
    setSubmittedParams({ q: location.trim(), checkIn, checkOut });
  }

  function handleSelectFacility(facilityId: string) {
    selectFacility(facilityId);
    createReservationMutation.reset();
  }

  function handleStartReservation(facilityId: string) {
    if (!submittedParams) return;

    createReservationMutation.mutate({
      checkIn: submittedParams.checkIn,
      checkOut: submittedParams.checkOut,
      facilityId,
    });
  }

  if (createReservationMutation.isSuccess && selectedFacility && submittedParams) {
    return (
      <ReservationCheckoutView
        checkIn={submittedParams.checkIn}
        checkOut={submittedParams.checkOut}
        facility={selectedFacility}
        reservation={createReservationMutation.data}
      />
    );
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

      {createReservationMutation.isError && (
        <Alert severity="error">
          {createReservationMutation.error instanceof Error
            ? createReservationMutation.error.message
            : t('reservation.createError')}
        </Alert>
      )}

      {result.facilities.length > 0 && (
        <ParkingResultsSection
          facilities={result.facilities}
          location={submittedParams?.q ?? location}
          onSelectFacility={handleSelectFacility}
          onStartReservation={handleStartReservation}
          reservationTimeLeft={reservationTimeLeft}
          selectedFacilityId={selectedFacilityId}
          startingReservationFacilityId={
            createReservationMutation.isPending ? createReservationMutation.variables?.facilityId ?? null : null
          }
        />
      )}
    </Stack>
  );
}
