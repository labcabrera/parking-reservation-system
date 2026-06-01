import { useEffect, useState } from 'react';
import type { FacilityResult } from '../types/catalog';

const RESERVATION_HOLD_MILLISECONDS = 10 * 60_000;

export function useParkingSelection(facilities?: FacilityResult[]) {
  const [selectedFacilityId, setSelectedFacilityId] = useState<string | null>(null);
  const [reservationExpiresAt, setReservationExpiresAt] = useState<number | null>(null);
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    if (!reservationExpiresAt) return undefined;

    const intervalId = window.setInterval(() => setNow(Date.now()), 1_000);

    return () => window.clearInterval(intervalId);
  }, [reservationExpiresAt]);

  useEffect(() => {
    setSelectedFacilityId(null);
    setReservationExpiresAt(null);
  }, [facilities]);

  function selectFacility(facilityId: string) {
    setSelectedFacilityId(facilityId);
    setReservationExpiresAt(Date.now() + RESERVATION_HOLD_MILLISECONDS);
    setNow(Date.now());
  }

  return {
    reservationTimeLeft: reservationExpiresAt ? reservationExpiresAt - now : 0,
    selectedFacilityId,
    selectFacility,
  };
}
