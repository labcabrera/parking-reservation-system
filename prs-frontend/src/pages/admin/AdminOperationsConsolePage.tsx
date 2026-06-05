import RefreshIcon from '@mui/icons-material/Refresh';
import SearchIcon from '@mui/icons-material/Search';
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getOrder } from '../../services/admin/ordersApi';
import { listReservations } from '../../services/admin/reservationsApi';
import type { Order } from '../../types/order';
import type { ReservationResponse } from '../../types/reservation';
import { formatPrice } from '../../utils/formatters';

const MAX_RANGE_DAYS = 40;

function toDateTimeInputValue(date: Date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 16);
}

function addDays(date: Date, days: number) {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function getRangeDays(start: string, end: string) {
  return (new Date(end).getTime() - new Date(start).getTime()) / 86_400_000;
}

export default function AdminOperationsConsolePage() {
  const { t } = useTranslation();
  const now = useMemo(() => new Date(), []);
  const [start, setStart] = useState(toDateTimeInputValue(addDays(now, -7)));
  const [end, setEnd] = useState(toDateTimeInputValue(addDays(now, 14)));
  const [orderIdInput, setOrderIdInput] = useState('');
  const [submittedOrderId, setSubmittedOrderId] = useState<string | null>(null);
  const rangeDays = getRangeDays(start, end);
  const isInvalidRange = rangeDays <= 0 || rangeDays > MAX_RANGE_DAYS;

  const reservationsQuery = useQuery({
    enabled: !isInvalidRange,
    queryFn: () => listReservations({ end, page: 0, size: 50, start }),
    queryKey: ['admin-reservations', { end, page: 0, size: 50, start }],
  });
  const orderQuery = useQuery({
    enabled: submittedOrderId !== null,
    queryFn: () => getOrder(submittedOrderId!),
    queryKey: ['admin-order', submittedOrderId],
  });

  function handleOrderSubmit(event: React.FormEvent) {
    event.preventDefault();
    const value = orderIdInput.trim();
    if (!value) return;
    setSubmittedOrderId(value);
  }

  const reservations = reservationsQuery.data?.content ?? [];

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' } }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography component="h1" variant="h1">
            {t('admin.operations.title')}
          </Typography>
          <Typography color="text.secondary">{t('admin.operations.subtitle')}</Typography>
        </Box>
        <Button
          disabled={reservationsQuery.isFetching || isInvalidRange}
          onClick={() => void reservationsQuery.refetch()}
          startIcon={<RefreshIcon />}
          variant="outlined"
        >
          {t('admin.refresh')}
        </Button>
      </Stack>

      <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, p: 2.5 }}>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
          <TextField
            error={isInvalidRange}
            fullWidth
            label={t('admin.operations.filters.start')}
            onChange={(event) => setStart(event.target.value)}
            type="datetime-local"
            value={start}
          />
          <TextField
            error={isInvalidRange}
            fullWidth
            helperText={isInvalidRange ? t('admin.operations.filters.rangeError', { days: MAX_RANGE_DAYS }) : ' '}
            label={t('admin.operations.filters.end')}
            onChange={(event) => setEnd(event.target.value)}
            type="datetime-local"
            value={end}
          />
        </Stack>
      </Paper>

      {reservationsQuery.isError && <Alert severity="error">{reservationsQuery.error.message}</Alert>}

      <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', xl: 'minmax(0, 1.4fr) 420px' } }}>
        <ReservationsPanel isLoading={reservationsQuery.isLoading || reservationsQuery.isFetching} reservations={reservations} />
        <OrderLookupPanel
          isLoading={orderQuery.isFetching}
          onOrderIdChange={setOrderIdInput}
          onSubmit={handleOrderSubmit}
          order={orderQuery.data ?? null}
          orderId={orderIdInput}
          queryError={orderQuery.isError ? orderQuery.error.message : null}
        />
      </Box>
    </Stack>
  );
}

function ReservationsPanel({
  isLoading,
  reservations,
}: {
  isLoading: boolean;
  reservations: ReservationResponse[];
}) {
  const { t } = useTranslation();

  return (
    <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, overflow: 'hidden' }}>
      <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'space-between', p: 2 }}>
        <Typography component="h2" variant="h2" sx={{ fontSize: 22 }}>
          {t('admin.operations.reservations.title')}
        </Typography>
        {isLoading && <CircularProgress size={22} />}
      </Box>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('admin.operations.reservations.id')}</TableCell>
              <TableCell>{t('admin.operations.reservations.facility')}</TableCell>
              <TableCell>{t('admin.operations.reservations.period')}</TableCell>
              <TableCell align="right">{t('admin.operations.reservations.price')}</TableCell>
              <TableCell>{t('admin.operations.reservations.status')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {reservations.map((reservation) => (
              <TableRow hover key={reservation.id ?? reservation.reservationId}>
                <TableCell>
                  <Typography sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                    {reservation.id ?? reservation.reservationId}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Typography sx={{ fontFamily: 'monospace', fontSize: 12 }}>{reservation.facilityId}</Typography>
                </TableCell>
                <TableCell>
                  <Typography sx={{ fontSize: 12 }}>{reservation.checkIn}</Typography>
                  <Typography color="text.secondary" sx={{ fontSize: 12 }}>
                    {reservation.checkOut}
                  </Typography>
                </TableCell>
                <TableCell align="right">
                  {reservation.estimatedPrice == null ? '-' : formatPrice(reservation.estimatedPrice)}
                </TableCell>
                <TableCell>
                  <Chip label={reservation.status ?? '-'} size="small" />
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && reservations.length === 0 && (
              <TableRow>
                <TableCell colSpan={5}>
                  <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                    {t('admin.operations.reservations.empty')}
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}

function OrderLookupPanel({
  isLoading,
  onOrderIdChange,
  onSubmit,
  order,
  orderId,
  queryError,
}: {
  isLoading: boolean;
  onOrderIdChange: (value: string) => void;
  onSubmit: (event: React.FormEvent) => void;
  order: Order | null;
  orderId: string;
  queryError: string | null;
}) {
  const { t } = useTranslation();

  return (
    <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, p: 2.5 }}>
      <Stack spacing={2}>
        <Box>
          <Typography component="h2" variant="h2" sx={{ fontSize: 22 }}>
            {t('admin.operations.orders.title')}
          </Typography>
          <Typography color="text.secondary">{t('admin.operations.orders.subtitle')}</Typography>
        </Box>

        <Stack component="form" direction="row" spacing={1} onSubmit={onSubmit}>
          <TextField
            fullWidth
            label={t('admin.operations.orders.id')}
            onChange={(event) => onOrderIdChange(event.target.value)}
            value={orderId}
          />
          <Button disabled={isLoading} startIcon={isLoading ? <CircularProgress color="inherit" size={18} /> : <SearchIcon />} type="submit" variant="contained">
            {t('admin.operations.orders.search')}
          </Button>
        </Stack>

        {queryError && <Alert severity="error">{queryError}</Alert>}

        {order && (
          <Stack spacing={1.5} sx={{ borderTop: 1, borderColor: 'divider', pt: 2 }}>
            <Typography sx={{ fontWeight: 900 }}>{order.id}</Typography>
            <SummaryRow label={t('admin.operations.orders.holdId')} value={order.holdId} />
            <SummaryRow label={t('admin.operations.orders.status')} value={order.status} />
            <SummaryRow label={t('admin.operations.orders.amount')} value={formatPrice(order.amount)} />
            <SummaryRow label={t('admin.operations.orders.expiresAt')} value={order.expiresAt} />
            {order.failureReason && <SummaryRow label={t('admin.operations.orders.failureReason')} value={order.failureReason} />}
          </Stack>
        )}
      </Stack>
    </Paper>
  );
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" spacing={2} sx={{ justifyContent: 'space-between' }}>
      <Typography color="text.secondary" sx={{ fontSize: 13, fontWeight: 700 }}>
        {label}
      </Typography>
      <Typography sx={{ fontSize: 13, overflowWrap: 'anywhere', textAlign: 'right' }}>{value}</Typography>
    </Stack>
  );
}
