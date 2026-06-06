import LocalParkingIcon from '@mui/icons-material/LocalParking';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import RuleIcon from '@mui/icons-material/Rule';
import { Box, Card, CardActionArea, CardContent, Chip, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { Link as RouterLink } from 'react-router-dom';
import { listParkingFacilities } from '../../services/catalogApi';
import { listOrders } from '../../services/admin/ordersApi';
import { listPricingRules } from '../../services/admin/pricingApi';
import { listReservations } from '../../services/admin/reservationsApi';

type AdminAreaMetric = 'facilities' | 'operations' | 'pricing';

function toDateTimeInputValue(date: Date) {
  const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60_000);
  return offsetDate.toISOString().slice(0, 16);
}

function addDays(date: Date, days: number) {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function getCurrentWeekStart() {
  const today = new Date();
  const day = today.getDay() === 0 ? 7 : today.getDay();
  const weekStart = new Date(today);
  weekStart.setDate(today.getDate() - day + 1);
  weekStart.setHours(0, 0, 0, 0);

  return weekStart;
}

const adminAreas = [
  {
    descriptionKey: 'admin.home.facilitiesDescription',
    icon: <LocalParkingIcon />,
    metricKey: 'facilities',
    titleKey: 'admin.home.facilities',
    to: '/admin/facilities',
  },
  {
    descriptionKey: 'admin.home.pricingDescription',
    icon: <RuleIcon />,
    metricKey: 'pricing',
    titleKey: 'admin.home.pricing',
    to: '/admin/pricing',
  },
  {
    descriptionKey: 'admin.home.operationsDescription',
    icon: <ReceiptLongIcon />,
    metricKey: 'operations',
    titleKey: 'admin.home.operations',
    to: '/admin/operations',
  },
] as const satisfies Array<{
  descriptionKey: string;
  icon: ReactNode;
  metricKey: AdminAreaMetric;
  titleKey: string;
  to: string;
}>;

export default function AdminHomePage() {
  const { t } = useTranslation();
  const reservationWindowStart = getCurrentWeekStart();
  const reservationWindowEnd = addDays(reservationWindowStart, 14);

  const facilitiesQuery = useQuery({
    queryFn: () => listParkingFacilities({ page: 0, size: 1 }),
    queryKey: ['admin-home', 'parking-facilities'],
  });
  const pricingQuery = useQuery({
    queryFn: () => listPricingRules(0, 1),
    queryKey: ['admin-home', 'pricing-rules'],
  });
  const reservationsQuery = useQuery({
    queryFn: () =>
      listReservations({
        end: toDateTimeInputValue(reservationWindowEnd),
        page: 0,
        size: 1,
        start: toDateTimeInputValue(reservationWindowStart),
      }),
    queryKey: ['admin-home', 'reservations', toDateTimeInputValue(reservationWindowStart)],
  });
  const ordersQuery = useQuery({
    queryFn: () => listOrders(0, 1),
    queryKey: ['admin-home', 'orders'],
  });

  const areaMetrics: Record<AdminAreaMetric, string> = {
    facilities: facilitiesQuery.isError
      ? t('admin.home.status.unavailable')
      : t('admin.home.status.facilities', {
          count: facilitiesQuery.data?.pagination.totalElements ?? 0,
        }),
    operations: reservationsQuery.isError || ordersQuery.isError
      ? t('admin.home.status.unavailable')
      : t('admin.home.status.operations', {
          orders: ordersQuery.data?.pagination.totalElements ?? 0,
          reservations: reservationsQuery.data?.pagination.totalElements ?? 0,
        }),
    pricing: pricingQuery.isError
      ? t('admin.home.status.unavailable')
      : t('admin.home.status.pricing', {
          count: pricingQuery.data?.pagination.totalElements ?? 0,
        }),
  };

  return (
    <Stack spacing={3}>
      <Box>
        <Typography component="h1" variant="h1">
          {t('admin.title')}
        </Typography>
        <Typography color="text.secondary">{t('admin.subtitle')}</Typography>
      </Box>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' } }}>
        {adminAreas.map((area) => (
          <Card key={area.to}>
            <CardActionArea component={RouterLink} to={area.to} sx={{ minHeight: 178 }}>
              <CardContent>
                <Stack spacing={2}>
                  <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
                    <Box sx={{ color: 'secondary.main', display: 'flex' }}>{area.icon}</Box>
                    <Chip label={areaMetrics[area.metricKey]} size="small" sx={{ maxWidth: 170 }} />
                  </Stack>
                  <Box>
                    <Typography component="h2" sx={{ fontSize: 20, fontWeight: 900 }}>
                      {t(area.titleKey)}
                    </Typography>
                    <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                      {t(area.descriptionKey)}
                    </Typography>
                  </Box>
                </Stack>
              </CardContent>
            </CardActionArea>
          </Card>
        ))}
      </Box>
    </Stack>
  );
}
