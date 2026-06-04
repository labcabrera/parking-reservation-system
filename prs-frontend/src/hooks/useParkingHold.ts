import { useState, useEffect, useRef, useCallback } from 'react';
import type { HoldResponse, HoldStatus } from '../types/hold';
import { getHold } from '../services/holdApi';

const POLLING_INTERVAL_MS = 500;
const POLLING_STATES: HoldStatus[] = ['PENDING_PRICE'];
const ERROR_STATES: HoldStatus[] = ['EXPIRED', 'FAILED'];

interface UseParkingHoldResult {
  hold: HoldResponse | null;
  loading: boolean;
  error: string | null;
  isPolling: boolean;
  /** Set when the hold has expired or pricing failed — prompt user to restart search */
  terminalMessage: string | null;
  refresh: () => void;
}

/**
 * Hook to track hold state. Polls every 500ms while status is PENDING_PRICE.
 * Stops polling once a terminal or active state is reached.
 */
export function useParkingHold(holdId: string | null): UseParkingHoldResult {
  const [hold, setHold] = useState<HoldResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [terminalMessage, setTerminalMessage] = useState<string | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetchHold = useCallback(async () => {
    if (!holdId) return;
    try {
      const data = await getHold(holdId);
      setHold(data);
      setError(null);

      // Stop polling if no longer in a polling state
      if (!POLLING_STATES.includes(data.status)) {
        if (intervalRef.current !== null) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
        // Surface terminal state message to prompt user action
        if (ERROR_STATES.includes(data.status)) {
          setTerminalMessage(
            data.status === 'EXPIRED'
              ? 'Your hold has expired. Please restart your search.'
              : 'Pricing could not be calculated. Please try again.',
          );
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
      if (intervalRef.current !== null) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    }
  }, [holdId]);

  const refresh = useCallback(() => {
    fetchHold();
  }, [fetchHold]);

  useEffect(() => {
    if (!holdId) {
      setHold(null);
      setLoading(false);
      setError(null);
      return;
    }

    setLoading(true);
    getHold(holdId)
      .then((data) => {
        setHold(data);
        setLoading(false);

        if (POLLING_STATES.includes(data.status)) {
          intervalRef.current = setInterval(fetchHold, POLLING_INTERVAL_MS);
        }
      })
      .catch((err) => {
        setError(err instanceof Error ? err.message : 'Unknown error');
        setLoading(false);
      });

    return () => {
      if (intervalRef.current !== null) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [holdId, fetchHold]);

  const isPolling = intervalRef.current !== null;

  return { hold, loading, error, isPolling, terminalMessage, refresh };
}
