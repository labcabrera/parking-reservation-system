export const BOOKING_SESSION_HEADER = 'X-Booking-Session-Id';

const BOOKING_SESSION_STORAGE_KEY = 'parking.bookingSessionId';

let memoryBookingSessionId: string | null = null;

function createBookingSessionId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function getBookingSessionId() {
  if (memoryBookingSessionId) {
    return memoryBookingSessionId;
  }

  if (typeof window === 'undefined') {
    memoryBookingSessionId = createBookingSessionId();
    return memoryBookingSessionId;
  }

  try {
    const storedBookingSessionId = window.localStorage.getItem(BOOKING_SESSION_STORAGE_KEY);
    if (storedBookingSessionId) {
      memoryBookingSessionId = storedBookingSessionId;
      return storedBookingSessionId;
    }

    const bookingSessionId = createBookingSessionId();
    window.localStorage.setItem(BOOKING_SESSION_STORAGE_KEY, bookingSessionId);
    memoryBookingSessionId = bookingSessionId;
    return bookingSessionId;
  } catch {
    memoryBookingSessionId = createBookingSessionId();
    return memoryBookingSessionId;
  }
}

export function resetBookingSessionId() {
  memoryBookingSessionId = createBookingSessionId();

  if (typeof window === 'undefined') {
    return memoryBookingSessionId;
  }

  try {
    window.localStorage.setItem(BOOKING_SESSION_STORAGE_KEY, memoryBookingSessionId);
  } catch {
    // Keep the in-memory session when persistent storage is unavailable.
  }

  return memoryBookingSessionId;
}
