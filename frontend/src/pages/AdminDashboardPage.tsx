import RefreshIcon from '@mui/icons-material/Refresh';
import VisibilityIcon from '@mui/icons-material/Visibility';
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
  TextField,
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
import { formatPrice } from '../utils/formatters';

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

function getOccupancySummary(slots: InventorySlot[]) {
  const capacity = slots.reduce((total, slot) => total + slot.capacity, 0);
  const reserved = slots.reduce((total, slot) => total + slot.reserved, 0);
  const free = slots.reduce((total, slot) => total + slot.free, 0);
  const occupancy = capacity > 0 ? Math.round((reserved / capacity) * 100) : 0;

  return { capacity, free, occupancy, reserved };
}

export default function AdminDashboardPage() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const now = useMemo(() => new Date(), []);
  const [selectedFacilityId, setSelectedFacilityId] = useState<string | null>(null);
  const [start, setStart] = useState(toDateTimeInputValue(addDays(now, -10)));
  const [end, setEnd] = useState(toDateTimeInputValue(addDays(now, 30)));

  const facilitiesQuery = useQuery({
    queryFn: () => listParkingFacilities({ page: 0, size: 50 }),
    queryKey: ['parking-facilities', { page: 0, size: 50 }],
  });
  const facilities = facilitiesQuery.data?.content ?? [];
  const selectedFacility = facilities.find((facility) => facility.id === selectedFacilityId) ?? null;
  const rangeDays = getRangeDays(start, end);
  const isInvalidRange = rangeDays <= 0 || rangeDays > MAX_RANGE_DAYS;

  const inventoryQuery = useQuery({
    enabled: selectedFacilityId !== null && !isInvalidRange,
    queryFn: () => getFacilityInventory(selectedFacilityId!, start, end),
    queryKey: ['parking-facility-inventory', selectedFacilityId, start, end],
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

  const totalSpots = facilities.reduce((total, facility) => total + facility.totalSpots, 0);
  const activeFacilities = facilities.filter((facility) => facility.status === 'ACTIVE').length;
  const occupancySummary = getOccupancySummary(inventoryQuery.data ?? []);

  return (
    <Stack spacing={3}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' } }}>
        <Box sx={{ flexGrow: 1 }}>
          <Typography component="h1" variant="h1">
            {t('admin.title')}
          </Typography>
          <Typography color="text.secondary">{t('admin.subtitle')}</Typography>
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
            end={end}
            isInvalidRange={isInvalidRange}
            isLoading={inventoryQuery.isFetching}
            occupancySummary={occupancySummary}
            onEndChange={setEnd}
            onStartChange={setStart}
            selectedFacility={selectedFacility}
            slots={inventoryQuery.data ?? []}
            start={start}
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
              <TableCell align="right">{t('admin.facilities.actions')}</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {facilities.map((facility) => (
              <TableRow hover key={facility.id} selected={facility.id === selectedFacilityId}>
                <TableCell>
                  <Typography sx={{ fontWeight: 800 }}>{facility.name}</Typography>
                  <Typography color="text.secondary" sx={{ fontSize: 12 }}>
                    {facility.address}
                  </Typography>
                </TableCell>
                <TableCell>{facility.city}</TableCell>
                <TableCell align="right">{facility.totalSpots}</TableCell>
                <TableCell>
                  <Chip label={facility.status} size="small" />
                </TableCell>
                <TableCell align="right">{formatPrice(facility.pricingRule.estimatedDailyPrice)}</TableCell>
                <TableCell align="right">
                  <Tooltip title={t('admin.facilities.view')}>
                    <IconButton onClick={() => onSelectFacility(facility.id)} size="small">
                      <VisibilityIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && facilities.length === 0 && (
              <TableRow>
                <TableCell colSpan={6}>
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
  end,
  isInvalidRange,
  isLoading,
  occupancySummary,
  onEndChange,
  onStartChange,
  selectedFacility,
  slots,
  start,
}: {
  end: string;
  isInvalidRange: boolean;
  isLoading: boolean;
  occupancySummary: ReturnType<typeof getOccupancySummary>;
  onEndChange: (value: string) => void;
  onStartChange: (value: string) => void;
  selectedFacility: ParkingFacility | null;
  slots: InventorySlot[];
  start: string;
}) {
  const { t } = useTranslation();

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
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          error={isInvalidRange}
          fullWidth
          label={t('admin.occupancy.start')}
          onChange={(event) => onStartChange(event.target.value)}
          type="datetime-local"
          value={start}
        />
        <TextField
          error={isInvalidRange}
          fullWidth
          helperText={isInvalidRange ? t('admin.occupancy.rangeError', { days: MAX_RANGE_DAYS }) : ' '}
          label={t('admin.occupancy.end')}
          onChange={(event) => onEndChange(event.target.value)}
          type="datetime-local"
          value={end}
        />
      </Stack>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mb: 2 }}>
        <Chip label={t('admin.occupancy.reserved', { count: occupancySummary.reserved })} />
        <Chip label={t('admin.occupancy.free', { count: occupancySummary.free })} />
        <Chip color="secondary" label={t('admin.occupancy.percent', { count: occupancySummary.occupancy })} />
      </Stack>
      <OccupancyColumnsChart slots={slots} />
    </Paper>
  );
}
