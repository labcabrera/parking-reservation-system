import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import SearchIcon from '@mui/icons-material/Search';
import { Box, CircularProgress, IconButton, InputBase, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { DateTimePickerField } from '../forms/DateTimePickerField';

interface ParkingSearchFormProps {
  checkIn: string;
  checkOut: string;
  isLoading: boolean;
  location: string;
  onCheckInChange: (value: string) => void;
  onCheckOutChange: (value: string) => void;
  onLocationChange: (value: string) => void;
  onSubmit: (event: React.FormEvent) => void;
}

export function ParkingSearchForm({
  checkIn,
  checkOut,
  isLoading,
  location,
  onCheckInChange,
  onCheckOutChange,
  onLocationChange,
  onSubmit,
}: ParkingSearchFormProps) {
  const { t } = useTranslation();
  const today = startOfToday();
  const checkInDate = parseDateTimeDate(checkIn);
  const checkOutDate = parseDateTimeDate(checkOut);
  const checkoutMinDate = checkInDate && checkInDate > today ? checkInDate : today;
  const checkoutReservedDates = getReservedDateRange(checkInDate, checkOutDate);

  return (
    <Box
      component="form"
      onSubmit={onSubmit}
      sx={{
        bgcolor: 'background.paper',
        borderRadius: { xs: 4, md: 999 },
        boxShadow: '0 28px 80px rgba(0, 0, 0, 0.28)',
        color: 'text.primary',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1.2fr 1fr 1fr 112px' },
        maxWidth: 1060,
        overflow: 'visible',
      }}
    >
      <Box
        sx={{
          alignItems: 'center',
          borderBottom: { xs: 1, md: 0 },
          borderColor: 'divider',
          borderRight: { md: 1 },
          display: 'flex',
          gap: 1.5,
          px: { xs: 2.5, md: 3 },
          py: 2,
        }}
      >
        <LocationOnOutlinedIcon sx={{ color: 'text.secondary', fontSize: 22 }} />
        <Box sx={{ minWidth: 0, width: '100%' }}>
          <Typography color="text.secondary" variant="caption">
            {t('search.form.locationHelp')}
          </Typography>
          <InputBase
            fullWidth
            inputProps={{ 'aria-label': t('search.form.locationAria') }}
            onChange={(event) => onLocationChange(event.target.value)}
            placeholder={t('search.form.locationPlaceholder')}
            required
            sx={{ fontSize: 17, lineHeight: 1.2 }}
            value={location}
          />
        </Box>
      </Box>

      <Box sx={{ borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', borderRight: { md: 1 } }}>
        <DateTimePickerField
          helperText={t('search.form.checkInHelp')}
          label={t('search.form.checkInLabel')}
          minDate={today}
          onChange={onCheckInChange}
          value={checkIn}
        />
      </Box>
      <Box sx={{ borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', borderRight: { md: 1 } }}>
        <DateTimePickerField
          helperText={t('search.form.checkOutHelp')}
          label={t('search.form.checkOutLabel')}
          minDate={checkoutMinDate}
          onChange={onCheckOutChange}
          reservedDates={checkoutReservedDates}
          value={checkOut}
        />
      </Box>
      <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'center', p: { xs: 2, md: 1.5 } }}>
        <IconButton
          aria-label={t('common.search')}
          disabled={isLoading}
          type="submit"
          sx={{
            bgcolor: 'secondary.main',
            color: 'secondary.contrastText',
            height: 56,
            width: 56,
            '&:hover': { bgcolor: 'secondary.dark' },
          }}
        >
          {isLoading ? <CircularProgress color="inherit" size={22} /> : <SearchIcon />}
        </IconButton>
      </Box>
    </Box>
  );
}

function startOfToday() {
  const now = new Date();

  return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

function parseDateTimeDate(value: string) {
  if (!value) {
    return null;
  }

  const [datePart] = value.split('T');
  const [year, month, day] = datePart.split('-').map(Number);

  if (!year || !month || !day) {
    return null;
  }

  return new Date(year, month - 1, day);
}

function getReservedDateRange(checkInDate: Date | null, checkOutDate: Date | null) {
  if (!checkInDate) {
    return [];
  }

  const endDate = checkOutDate && checkOutDate >= checkInDate ? checkOutDate : checkInDate;
  const dates: Date[] = [];
  const cursor = new Date(checkInDate);

  while (cursor <= endDate) {
    dates.push(new Date(cursor));
    cursor.setDate(cursor.getDate() + 1);
  }

  return dates;
}
