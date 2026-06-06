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
import { selectCheckoutOption } from '../services/checkoutApi';
import type { SearchRequest } from '../types/catalog';
import type { SelectOptionRequest } from '../types/checkout';

export default function SearchPage() {
  const { t } = useTranslation();
  const [location, setLocation] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [submittedParams, setSubmittedParams] = useState<SearchRequest | null>(null);

  const { isLoading, isError, error, result } = useParkingSearch(submittedParams);
  const { reservationTimeLeft, selectedFacilityId, selectFacility } = useParkingSelection(result.facilities);
  const selectedFacility = result.facilities.find((facility) => facility.facilityId === selectedFacilityId) ?? null;
  const selectOptionMutation = useMutation({
    mutationFn: (request: SelectOptionRequest) => selectCheckoutOption(request),
  });

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    if (!location.trim() || !checkIn || !checkOut) return;

    selectOptionMutation.reset();
    setSubmittedParams({ q: location.trim(), checkIn, checkOut });
  }

  function handleSelectFacility(facilityId: string) {
    selectFacility(facilityId);
    selectOptionMutation.reset();
  }

  function handleCheckInChange(value: string) {
    setCheckIn(value);
    if (checkOut && !isCheckOutAfterCheckIn(value, checkOut)) {
      setCheckOut('');
    }
  }

  function handleStartCheckout(facilityId: string) {
    if (!submittedParams) return;

    selectOptionMutation.mutate({
      checkIn: submittedParams.checkIn,
      checkOut: submittedParams.checkOut,
      facilityId,
    });
  }

  if (selectOptionMutation.isSuccess && selectedFacility && submittedParams) {
    return (
      <ReservationCheckoutView
        checkIn={submittedParams.checkIn}
        checkOut={submittedParams.checkOut}
        checkout={selectOptionMutation.data}
        facility={selectedFacility}
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
          onCheckInChange={handleCheckInChange}
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

      {selectOptionMutation.isError && (
        <Alert severity="error">
          {selectOptionMutation.error instanceof Error
            ? selectOptionMutation.error.message
            : t('reservation.createError')}
        </Alert>
      )}

      {result.facilities.length > 0 && (
        <ParkingResultsSection
          facilities={result.facilities}
          location={submittedParams?.q ?? location}
          onSelectFacility={handleSelectFacility}
          onStartReservation={handleStartCheckout}
          reservationTimeLeft={reservationTimeLeft}
          selectedFacilityId={selectedFacilityId}
          startingReservationFacilityId={
            selectOptionMutation.isPending ? selectOptionMutation.variables?.facilityId ?? null : null
          }
        />
      )}
    </Stack>
  );
}

function isCheckOutAfterCheckIn(checkIn: string, checkOut: string) {
  return new Date(checkOut).getTime() > new Date(checkIn).getTime();
}
