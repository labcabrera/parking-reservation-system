import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import SyncAltIcon from '@mui/icons-material/SyncAlt';
import TimeToLeaveIcon from '@mui/icons-material/TimeToLeave';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Divider,
  FormControlLabel,
  Paper,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../auth/AuthContext';
import type { FacilityResult } from '../../types/catalog';
import type { ReservationResponse } from '../../types/reservation';
import { formatPrice } from '../../utils/formatters';

interface ReservationCheckoutViewProps {
  checkIn: string;
  checkOut: string;
  facility: FacilityResult;
  reservation: ReservationResponse;
}

export function ReservationCheckoutView({
  checkIn,
  checkOut,
  facility,
  reservation,
}: ReservationCheckoutViewProps) {
  const { i18n, t } = useTranslation();
  const { isAuthenticated } = useAuth();
  const days = getReservationDays(checkIn, checkOut);
  const totalAmount = getReservationTotal(reservation) ?? facility.estimatedPrice?.amount ?? facility.dailyRate;

  return (
    <Box sx={{ bgcolor: 'background.paper', borderRadius: 3, px: { xs: 2, md: 5 }, py: { xs: 4, md: 7 } }}>
      <Stack spacing={1} sx={{ alignItems: 'center', mb: { xs: 4, md: 7 }, textAlign: 'center' }}>
        {!isAuthenticated && <LoginPrompt />}
        <Typography component="h1" variant="h1" sx={{ color: '#27274f', fontSize: { xs: 34, md: 48 }, fontWeight: 900 }}>
          {t('checkout.title')}
        </Typography>
      </Stack>

      <Box
        sx={{
          display: 'grid',
          gap: { xs: 4, lg: 7 },
          gridTemplateColumns: { xs: '1fr', lg: 'minmax(0, 1.18fr) minmax(380px, 0.82fr)' },
        }}
      >
        <CheckoutDetailsForm />
        <ReservationSummary
          checkIn={checkIn}
          checkOut={checkOut}
          days={days}
          facility={facility}
          locale={i18n.language}
          reservation={reservation}
          totalAmount={totalAmount}
        />
      </Box>
    </Box>
  );
}

function LoginPrompt() {
  const { isConfigured, isLoading, login } = useAuth();
  const { t } = useTranslation();

  return (
    <Typography sx={{ color: 'text.primary', fontSize: { xs: 16, md: 20 } }}>
      <Tooltip title={isConfigured ? t('auth.loginTooltip') : t('auth.missingConfig')}>
        <span>
          <Button
            disabled={!isConfigured || isLoading}
            onClick={() => void login()}
            sx={{ color: 'secondary.main', font: 'inherit', minWidth: 0, p: 0, textDecoration: 'underline' }}
            type="button"
            variant="text"
          >
            {t('checkout.loginLink')}
          </Button>
        </span>
      </Tooltip>{' '}
      {t('checkout.loginSuffix')}
    </Typography>
  );
}

function CheckoutDetailsForm() {
  const { isAuthenticated, user } = useAuth();
  const { t } = useTranslation();
  const displayName = user?.profile.name ?? user?.profile.email ?? t('auth.defaultUser');
  const email = user?.profile.email;

  return (
    <Stack spacing={3.2}>
      <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography component="h2" sx={{ fontSize: 28, fontWeight: 900 }}>
          {t('checkout.customer.title')}
        </Typography>
        {!isAuthenticated && (
          <Typography sx={{ color: 'text.secondary', fontSize: 12, fontWeight: 700 }}>
            {t('checkout.requiredFields')}
          </Typography>
        )}
      </Stack>

      {isAuthenticated ? (
        <Paper
          elevation={0}
          sx={{
            border: 1,
            borderColor: 'divider',
            borderRadius: 2,
            p: 2.5,
          }}
        >
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <CheckCircleIcon color="secondary" />
            <Box>
              <Typography sx={{ fontSize: 18, fontWeight: 900 }}>{displayName}</Typography>
              {email && <Typography color="text.secondary">{email}</Typography>}
              <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                {t('checkout.customer.authenticated')}
              </Typography>
            </Box>
          </Stack>
        </Paper>
      ) : (
        <Stack spacing={2}>
          <TextField fullWidth label={t('checkout.customer.fullName')} required />
          <TextField fullWidth label={t('checkout.customer.email')} required type="email" />
        </Stack>
      )}

      <Box>
        <Typography component="h2" sx={{ fontSize: 28, fontWeight: 900, mb: 1 }}>
          {t('checkout.vehicle.title')}
        </Typography>
        <Typography sx={{ fontSize: 20, lineHeight: 1.35, mb: 3 }}>
          {t('checkout.vehicle.help')}
        </Typography>
        <Box
          sx={{
            border: 1,
            borderColor: 'divider',
            borderRadius: 1.5,
            display: 'grid',
            gridTemplateColumns: '48px minmax(0, 260px)',
            maxWidth: 320,
            overflow: 'hidden',
          }}
        >
          <Box sx={{ alignItems: 'center', bgcolor: '#0057b8', color: '#ffd700', display: 'flex', justifyContent: 'center' }}>
            <Typography sx={{ fontSize: 19, fontWeight: 900 }}>EU</Typography>
          </Box>
          <TextField
            hiddenLabel
            placeholder="XXXXXX"
            variant="standard"
            slotProps={{
              input: {
                disableUnderline: true,
                sx: { fontSize: 30, fontWeight: 800, letterSpacing: 1.5, px: 4, py: 1.6 },
              },
            }}
          />
        </Box>
      </Box>

      <Box>
        <Typography component="h2" sx={{ fontSize: 28, fontWeight: 900, mb: 1 }}>
          {t('checkout.invoice.title')}
        </Typography>
        <Stack spacing={1.5}>
          <FormControlLabel control={<Checkbox color="secondary" />} label={t('checkout.invoice.request')} />
          <FormControlLabel control={<Checkbox color="secondary" />} label={t('checkout.invoice.marketing')} />
          <FormControlLabel control={<Checkbox color="secondary" />} label={t('checkout.invoice.terms')} />
        </Stack>
      </Box>

      <Button
        color="secondary"
        endIcon={<ArrowForwardIcon />}
        size="large"
        sx={{ borderRadius: 999, fontSize: 18, justifyContent: 'space-between', mt: 1, px: 4, py: 1.8 }}
        variant="contained"
      >
        {t('checkout.payment')}
      </Button>
      <Typography align="center" color="text.secondary" sx={{ fontSize: 12 }}>
        {t('checkout.paymentHint')}
      </Typography>
    </Stack>
  );
}

interface ReservationSummaryProps {
  checkIn: string;
  checkOut: string;
  days: number;
  facility: FacilityResult;
  locale: string;
  reservation: ReservationResponse;
  totalAmount?: number;
}

function ReservationSummary({
  checkIn,
  checkOut,
  days,
  facility,
  locale,
  reservation,
  totalAmount,
}: ReservationSummaryProps) {
  const { t } = useTranslation();
  const baseAmount = totalAmount == null ? undefined : totalAmount * 1.19;
  const managementFee = totalAmount == null ? undefined : Math.max(totalAmount * 0.035, 1.5);
  const longStayDiscount =
    baseAmount == null || managementFee == null || totalAmount == null ? undefined : totalAmount - baseAmount - managementFee;
  const dailyAmount = totalAmount == null ? undefined : totalAmount / Math.max(days, 1);

  return (
    <Box sx={{ bgcolor: '#faf5ed', borderRadius: 3, p: { xs: 3, md: 4 } }}>
      <Stack spacing={3}>
        <Box>
          <Typography component="h2" sx={{ color: '#27274f', fontSize: { xs: 28, md: 32 }, fontWeight: 900, mb: 3 }}>
            {t('checkout.summary.title', { count: days })}
          </Typography>
          <Typography sx={{ fontSize: 17, fontWeight: 900 }}>
            {facility.city} - {facility.name}
          </Typography>
          <Typography sx={{ color: '#53618c', fontSize: 15 }}>{facility.address}</Typography>
        </Box>

        <Stack spacing={2}>
          <SummaryRow label={t('checkout.summary.checkIn')} value={formatDateTime(checkIn, locale)} />
          <SummaryRow label={t('checkout.summary.checkOut')} value={formatDateTime(checkOut, locale)} />
        </Stack>

        <Alert
          icon={<InfoOutlinedIcon fontSize="inherit" />}
          severity="info"
          sx={{
            bgcolor: '#eaf3ff',
            border: '1px solid #315cff',
            borderRadius: 1.5,
            color: '#53618c',
          }}
        >
          <Typography sx={{ color: '#1948dc', fontSize: 13, fontWeight: 900 }}>
            {t('checkout.summary.courtesyTitle', { count: days })}
          </Typography>
          <Typography sx={{ fontSize: 14 }}>{t('checkout.summary.courtesyText', { count: days })}</Typography>
        </Alert>

        <Divider sx={{ borderColor: 'secondary.main' }} />

        <Box>
          <SummaryRow label={t('checkout.summary.breakdown')} value="" />
          {baseAmount != null && <SummaryRow label={t('checkout.summary.baseAmount')} value={formatPrice(baseAmount)} />}
          {managementFee != null && <SummaryRow label={t('checkout.summary.managementFee')} value={formatPrice(managementFee)} />}
          {longStayDiscount != null && (
            <SummaryRow label={t('checkout.summary.longStayDiscount')} value={formatPrice(longStayDiscount)} />
          )}
        </Box>

        <Stack direction="row" sx={{ alignItems: 'baseline', justifyContent: 'space-between' }}>
          <Typography sx={{ fontSize: 30, fontWeight: 900 }}>
            {t('checkout.summary.total')}{' '}
            <Typography component="span" sx={{ fontSize: 13 }}>
              {t('checkout.summary.taxIncluded')}
            </Typography>
          </Typography>
          <Box sx={{ textAlign: 'right' }}>
            <Typography sx={{ fontSize: 30, fontWeight: 900 }}>
              {totalAmount == null ? t('results.priceUnavailable') : formatPrice(totalAmount)}
            </Typography>
            {dailyAmount != null && (
              <Typography sx={{ color: '#53618c', fontSize: 16 }}>{formatPrice(dailyAmount)} / dia</Typography>
            )}
          </Box>
        </Stack>

        {reservation.status && (
          <Typography color="text.secondary" sx={{ fontSize: 13 }}>
            {t('checkout.summary.status', { status: reservation.status })}
          </Typography>
        )}

        <Stack spacing={2.2} sx={{ pt: 5 }}>
          <Feature icon={<Typography sx={{ color: '#9aa3b5', fontSize: 13, fontWeight: 900 }}>2h</Typography>} title={t('checkout.features.courtesy.title')} text={t('checkout.features.courtesy.text')} />
          <Feature icon={<SyncAltIcon />} title={t('checkout.features.entries.title')} text={t('checkout.features.entries.text')} />
          <Feature icon={<TimeToLeaveIcon />} title={t('checkout.features.express.title')} text={t('checkout.features.express.text')} />
          <Feature icon={<ReceiptLongIcon />} title={t('checkout.features.cancel.title')} text={t('checkout.features.cancel.text')} />
        </Stack>
      </Stack>
    </Box>
  );
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" spacing={2} sx={{ justifyContent: 'space-between' }}>
      <Typography sx={{ fontSize: 16, fontWeight: value ? 700 : 900 }}>{label}</Typography>
      {value && <Typography sx={{ fontSize: 16, textAlign: 'right' }}>{value}</Typography>}
    </Stack>
  );
}

function Feature({ icon, text, title }: { icon: ReactNode; text: string; title: string }) {
  return (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'flex-start' }}>
      <Box sx={{ color: '#b2b9c9', minWidth: 22 }}>{icon}</Box>
      <Box>
        <Typography sx={{ fontWeight: 900 }}>{title}</Typography>
        <Typography sx={{ color: '#53618c', fontSize: 16 }}>{text}</Typography>
      </Box>
    </Stack>
  );
}

function getReservationDays(checkIn: string, checkOut: string) {
  const milliseconds = new Date(checkOut).getTime() - new Date(checkIn).getTime();
  return Math.max(Math.ceil(milliseconds / 86_400_000), 1);
}

function formatDateTime(value: string, locale: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat(locale, {
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    month: 'long',
    weekday: 'long',
  }).format(date);
}

function getReservationTotal(reservation: ReservationResponse) {
  const estimatedTotal = reservation.estimatedTotal;
  const totalCharged = reservation.totalCharged;

  if (isMoneyLike(estimatedTotal)) return Number(estimatedTotal.amount);
  if (isMoneyLike(totalCharged)) return Number(totalCharged.amount);

  return undefined;
}

function isMoneyLike(value: unknown): value is { amount: number | string } {
  const candidate = value as { amount?: unknown } | null;

  return Boolean(
    candidate &&
      typeof candidate === 'object' &&
      (typeof candidate.amount === 'number' || typeof candidate.amount === 'string') &&
      !Number.isNaN(Number(candidate.amount)),
  );
}
