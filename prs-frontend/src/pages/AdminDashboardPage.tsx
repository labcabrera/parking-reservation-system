import NavigateBeforeIcon from '@mui/icons-material/NavigateBefore';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import RefreshIcon from '@mui/icons-material/Refresh';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { OccupancyColumnsChart } from '../components/admin/OccupancyColumnsChart';
import { ParkingFacilityForm } from '../components/admin/ParkingFacilityForm';
import {
  createParkingFacility,
  getFacilityInventory,
  listParkingFacilities,
} from '../services/catalogApi';
import type { CreateParkingFacilityRequest, InventorySlot, ParkingFacility } from '../types/catalog';
import {
  getFacilityCapacityLongTerm,
  getFacilityCapacityShortTerm,
  getFacilityCapacityTotal,
} from '../types/catalog';
import { formatPrice } from '../utils/formatters';

const INVENTORY_WINDOW_DAYS = 14;

function toDateTimeInputValue(date: Date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 16);
}

function getCurrentWeekStart() {
  const today = new Date();
  const day = today.getDay() === 0 ? 7 : today.getDay();
  const weekStart = new Date(today);
  weekStart.setDate(today.getDate() - day + 1);
  weekStart.setHours(0, 0, 0, 0);

  return weekStart;
}

function addDays(date: Date, days: number) {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function getInventoryWindow(weekOffset: number) {
  const start = addDays(getCurrentWeekStart(), weekOffset * 7);
  const end = addDays(start, INVENTORY_WINDOW_DAYS);

  return {
    end,
    endValue: toDateTimeInputValue(end),
    start,
    startValue: toDateTimeInputValue(start),
  };
}

function formatWindowLabel(start: Date, end: Date) {
  const formatter = new Intl.DateTimeFormat(undefined, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });

  return `${formatter.format(start)} - ${formatter.format(end)}`;
}

function getOccupancySummary(slots: InventorySlot[]) {
  const capacity = slots.reduce((total, slot) => total + slot.capacity, 0);
  const reserved = slots.reduce((total, slot) => total + slot.reserved, 0);
  const free = slots.reduce((total, slot) => total + slot.free, 0);
  const occupancy = capacity > 0 ? Math.round((reserved / capacity) * 100) : 0;

  return { capacity, free, occupancy, reserved };
}

function splitInventorySlots(slots: InventorySlot[]) {
  return {
    longTerm: slots.filter((slot) => slot.blockType === 'LONG_TERM'),
    shortTerm: slots.filter((slot) => slot.blockType === 'SHORT_TERM' || slot.blockType == null),
  };
}

export default function AdminDashboardPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [selectedFacilityId, setSelectedFacilityId] = useState<string | null>(null);
  const [weekOffset, setWeekOffset] = useState(0);
  const inventoryWindow = useMemo(() => getInventoryWindow(weekOffset), [weekOffset]);

  const facilitiesQuery = useQuery({
    queryFn: () => listParkingFacilities({ page: 0, size: 50 }),
    queryKey: ['parking-facilities', { page: 0, size: 50 }],
  });
  const facilities = facilitiesQuery.data?.content ?? [];
  const selectedFacility = facilities.find((facility) => facility.id === selectedFacilityId) ?? null;

  const inventoryQuery = useQuery({
    enabled: selectedFacilityId !== null,
    queryFn: () => getFacilityInventory(selectedFacilityId!, inventoryWindow.startValue, inventoryWindow.endValue),
    queryKey: ['parking-facility-inventory', selectedFacilityId, inventoryWindow.startValue, inventoryWindow.endValue],
  });

  const createFacilityMutation = useMutation({
    mutationFn: (request: CreateParkingFacilityRequest) => createParkingFacility(request),
    onSuccess: async (facility) => {
      setSelectedFacilityId(facility.id);
      await queryClient.invalidateQueries({ queryKey: ['parking-facilities'] });
    },
  });

  useEffect(() => {
    if (!selectedFacilityId && facilities.length > 0) {
      setSelectedFacilityId(facilities[0].id);
    }
  }, [facilities, selectedFacilityId]);

  const totalSpots = facilities.reduce((total, facility) => total + getFacilityCapacityTotal(facility), 0);
  const activeFacilities = facilities.filter((facility) => facility.status === 'ACTIVE').length;
  const occupancySummary = getOccupancySummary(inventoryQuery.data ?? []);

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' } }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography component="h1" variant="h1">
            {t('admin.facilities.adminTitle')}
          </Typography>
          <Typography color="text.secondary">{t('admin.facilities.adminSubtitle')}</Typography>
        </Box>
        <Button
          disabled={facilitiesQuery.isFetching}
          onClick={() => void facilitiesQuery.refetch()}
          startIcon={<RefreshIcon />}
          variant="outlined"
        >
          {t('admin.refresh')}
        </Button>
      </Stack>

      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
        <MetricCard label={t('admin.metrics.facilities')} value={String(facilities.length)} />
        <MetricCard label={t('admin.metrics.active')} value={String(activeFacilities)} />
        <MetricCard label={t('admin.metrics.spots')} value={String(totalSpots)} />
        <MetricCard label={t('admin.metrics.occupancy')} value={`${occupancySummary.occupancy}%`} />
      </Stack>

      {facilitiesQuery.isError && (
        <Alert severity="error">{facilitiesQuery.error.message}</Alert>
      )}
      {createFacilityMutation.isError && (
        <Alert severity="error">{createFacilityMutation.error.message}</Alert>
      )}
      {createFacilityMutation.isSuccess && (
        <Alert severity="success">{t('admin.form.created')}</Alert>
      )}

      <Box sx={{ display: 'grid', gap: 3, gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1.35fr) 420px' } }}>
        <Stack spacing={3}>
          <FacilitiesTable
            facilities={facilities}
            isLoading={facilitiesQuery.isLoading}
            onSelectFacility={setSelectedFacilityId}
            selectedFacilityId={selectedFacilityId}
          />
          <OccupancyPanel
            isLoading={inventoryQuery.isFetching}
            occupancySummary={occupancySummary}
            onNextWeek={() => setWeekOffset((current) => current + 1)}
            onPreviousWeek={() => setWeekOffset((current) => current - 1)}
            selectedFacility={selectedFacility}
            slots={inventoryQuery.data ?? []}
            windowLabel={formatWindowLabel(inventoryWindow.start, inventoryWindow.end)}
          />
        </Stack>

        <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, p: 2.5 }}>
          <Typography component="h2" variant="h2" sx={{ fontSize: 22, mb: 2 }}>
            {t('admin.form.title')}
          </Typography>
          <ParkingFacilityForm
            isSubmitting={createFacilityMutation.isPending}
            onSubmit={(request) => createFacilityMutation.mutate(request)}
          />
        </Paper>
      </Box>
    </Stack>
  );
}

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <Card sx={{ flex: 1 }}>
      <CardContent sx={{ '&:last-child': { pb: 2 }, p: 2 }}>
        <Typography color="text.secondary" sx={{ fontSize: 13, fontWeight: 700 }}>
          {label}
        </Typography>
        <Typography sx={{ fontSize: 28, fontWeight: 900 }}>{value}</Typography>
      </CardContent>
    </Card>
  );
}

function FacilitiesTable({
  facilities,
  isLoading,
  onSelectFacility,
  selectedFacilityId,
}: {
  facilities: ParkingFacility[];
  isLoading: boolean;
  onSelectFacility: (facilityId: string) => void;
  selectedFacilityId: string | null;
}) {
  const { t } = useTranslation();

  function handleFacilityKeyDown(event: React.KeyboardEvent<HTMLTableRowElement>, facilityId: string) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onSelectFacility(facilityId);
    }
  }

  return (
    <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, overflow: 'hidden' }}>
      <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'space-between', p: 2 }}>
        <Typography component="h2" variant="h2" sx={{ fontSize: 22 }}>
          {t('admin.facilities.title')}
        </Typography>
        {isLoading && <CircularProgress size={22} />}
      </Box>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>{t('admin.facilities.name')}</TableCell>
              <TableCell>{t('admin.facilities.city')}</TableCell>
              <TableCell align="right">{t('admin.facilities.spots')}</TableCell>
              <TableCell>{t('admin.facilities.status')}</TableCell>
              <TableCell align="right">{t('admin.facilities.price')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {facilities.map((facility) => (
              <TableRow
                hover
                key={facility.id}
                onClick={() => onSelectFacility(facility.id)}
                onKeyDown={(event) => handleFacilityKeyDown(event, facility.id)}
                role="button"
                selected={facility.id === selectedFacilityId}
                sx={{ cursor: 'pointer' }}
                tabIndex={0}
              >
                <TableCell>
                  <Typography sx={{ fontWeight: 800 }}>{facility.name}</Typography>
                  <Typography color="text.secondary" sx={{ fontSize: 12 }}>
                    {facility.address}
                  </Typography>
                </TableCell>
                <TableCell>{facility.city}</TableCell>
                <TableCell align="right">
                  <Typography sx={{ fontWeight: 800 }}>{getFacilityCapacityTotal(facility)}</Typography>
                  <Typography color="text.secondary" sx={{ fontSize: 12 }}>
                    {t('admin.facilities.capacityBreakdown', {
                      longTerm: getFacilityCapacityLongTerm(facility),
                      shortTerm: getFacilityCapacityShortTerm(facility),
                    })}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Chip label={facility.status} size="small" />
                </TableCell>
                <TableCell align="right">{formatPrice(facility.pricingRule.estimatedDailyPrice)}</TableCell>
              </TableRow>
            ))}
            {!isLoading && facilities.length === 0 && (
              <TableRow>
                <TableCell colSpan={5}>
                  <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                    {t('admin.facilities.empty')}
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

function OccupancyPanel({
  isLoading,
  occupancySummary,
  onNextWeek,
  onPreviousWeek,
  selectedFacility,
  slots,
  windowLabel,
}: {
  isLoading: boolean;
  occupancySummary: ReturnType<typeof getOccupancySummary>;
  onNextWeek: () => void;
  onPreviousWeek: () => void;
  selectedFacility: ParkingFacility | null;
  slots: InventorySlot[];
  windowLabel: string;
}) {
  const { t } = useTranslation();
  const inventorySlots = splitInventorySlots(slots);

  return (
    <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', borderRadius: 2, p: 2.5 }}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' }, mb: 2 }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography component="h2" variant="h2" sx={{ fontSize: 22 }}>
            {t('admin.occupancy.title')}
          </Typography>
          <Typography color="text.secondary">
            {selectedFacility ? selectedFacility.name : t('admin.occupancy.noFacility')}
          </Typography>
        </Box>
        {isLoading && <CircularProgress size={22} />}
      </Stack>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={1.5}
        sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between', mb: 2 }}
      >
        <Box>
          <Typography sx={{ fontSize: 13, fontWeight: 800 }}>{t('admin.occupancy.window')}</Typography>
          <Typography color="text.secondary">{windowLabel}</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Tooltip title={t('admin.occupancy.previousWeek')}>
            <IconButton onClick={onPreviousWeek}>
              <NavigateBeforeIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title={t('admin.occupancy.nextWeek')}>
            <IconButton onClick={onNextWeek}>
              <NavigateNextIcon />
            </IconButton>
          </Tooltip>
        </Stack>
      </Stack>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <Chip label={t('admin.occupancy.reserved', { count: occupancySummary.reserved })} />
        <Chip label={t('admin.occupancy.free', { count: occupancySummary.free })} />
        <Chip color="secondary" label={t('admin.occupancy.percent', { count: occupancySummary.occupancy })} />
      </Stack>
      <Stack spacing={2}>
        <OccupancyBlock title={t('admin.occupancy.shortTerm')} slots={inventorySlots.shortTerm} />
        <OccupancyBlock title={t('admin.occupancy.longTerm')} slots={inventorySlots.longTerm} />
      </Stack>
    </Paper>
  );
}

function OccupancyBlock({ slots, title }: { slots: InventorySlot[]; title: string }) {
  const { t } = useTranslation();
  const summary = getOccupancySummary(slots);

  return (
    <Box>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={1}
        sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between', mb: 1 }}
      >
        <Typography component="h3" sx={{ fontSize: 16, fontWeight: 900 }}>
          {title}
        </Typography>
        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', rowGap: 1 }}>
          <Chip label={t('admin.occupancy.reserved', { count: summary.reserved })} size="small" />
          <Chip label={t('admin.occupancy.free', { count: summary.free })} size="small" />
          <Chip color="secondary" label={t('admin.occupancy.percent', { count: summary.occupancy })} size="small" />
        </Stack>
      </Stack>
      <OccupancyColumnsChart slots={slots} />
    </Box>
  );
}
