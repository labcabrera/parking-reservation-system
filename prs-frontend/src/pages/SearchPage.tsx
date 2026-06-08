import { useState } from 'react';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import ReplayIcon from '@mui/icons-material/Replay';
import { Alert, Box, Button, Chip, Paper, Stack, Typography } from '@mui/material';
import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
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
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const paymentStatus = searchParams.get('paymentStatus');
  const paymentOrderId = searchParams.get('orderId');
  const paymentAttemptId = searchParams.get('attemptId');
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

  if (paymentStatus) {
    return (
      <PaymentResultView
        attemptId={paymentAttemptId}
        onBackToSearch={() => navigate('/', { replace: true })}
        orderId={paymentOrderId}
        status={paymentStatus}
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

type PaymentResultViewProps = {
  attemptId: string | null;
  onBackToSearch: () => void;
  orderId: string | null;
  status: string;
};

function PaymentResultView({ attemptId, onBackToSearch, orderId, status }: PaymentResultViewProps) {
  const { t } = useTranslation();
  const normalizedStatus = status.toUpperCase();
  const isSuccess = normalizedStatus === 'SUCCESS' || normalizedStatus === 'PAID';

  return (
    <Paper
      elevation={0}
      sx={{
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 3,
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          bgcolor: isSuccess ? 'success.light' : 'error.light',
          color: isSuccess ? 'success.contrastText' : 'error.contrastText',
          p: { xs: 3, md: 5 },
        }}
      >
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: 'flex-start' }}>
          {isSuccess ? <CheckCircleIcon fontSize="large" /> : <InfoOutlinedIcon fontSize="large" />}
          <Stack spacing={1}>
            <Chip
              color={isSuccess ? 'success' : 'error'}
              label={normalizedStatus}
              size="small"
              sx={{ alignSelf: 'flex-start', fontWeight: 800 }}
            />
            <Typography component="h1" variant="h3">
              {isSuccess ? t('paymentResult.successTitle') : t('paymentResult.errorTitle')}
            </Typography>
            <Typography sx={{ maxWidth: 720 }} variant="h6">
              {isSuccess ? t('paymentResult.successSubtitle') : t('paymentResult.errorSubtitle')}
            </Typography>
          </Stack>
        </Stack>
      </Box>

      <Stack spacing={3} sx={{ p: { xs: 3, md: 5 } }}>
        <Typography color="text.secondary" variant="body1">
          {isSuccess ? t('paymentResult.successBody') : t('paymentResult.errorBody')}
        </Typography>

        <Stack
          direction={{ xs: 'column', md: 'row' }}
          spacing={2}
          sx={{
            '& > *': {
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 2,
              flex: 1,
              minWidth: 0,
              p: 2,
            },
          }}
        >
          <Stack spacing={0.75}>
            <Typography color="text.secondary" variant="overline">
              {t('paymentResult.orderId')}
            </Typography>
            <Typography sx={{ overflowWrap: 'anywhere' }} variant="body2">
              {orderId ?? t('paymentResult.unavailable')}
            </Typography>
          </Stack>
          <Stack spacing={0.75}>
            <Typography color="text.secondary" variant="overline">
              {t('paymentResult.attemptId')}
            </Typography>
            <Typography sx={{ overflowWrap: 'anywhere' }} variant="body2">
              {attemptId ?? t('paymentResult.unavailable')}
            </Typography>
          </Stack>
        </Stack>

        <Button
          color="primary"
          onClick={onBackToSearch}
          size="large"
          startIcon={<ReplayIcon />}
          sx={{ alignSelf: 'flex-start' }}
          variant="contained"
        >
          {t('paymentResult.backToSearch')}
        </Button>
      </Stack>
    </Paper>
  );
}
