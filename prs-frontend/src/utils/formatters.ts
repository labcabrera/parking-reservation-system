const currencyFormatter = new Intl.NumberFormat('es-ES', {
  currency: 'EUR',
  maximumFractionDigits: 2,
  style: 'currency',
});

export function formatPrice(value: number) {
  return currencyFormatter.format(value).replace(/\s/g, '');
}

export function formatReservationTime(milliseconds: number) {
  const safeMilliseconds = Math.max(milliseconds, 0);
  const minutes = Math.floor(safeMilliseconds / 60_000);
  const seconds = Math.floor((safeMilliseconds % 60_000) / 1_000);

  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}
