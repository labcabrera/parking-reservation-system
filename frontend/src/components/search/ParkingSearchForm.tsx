import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import SearchIcon from '@mui/icons-material/Search';
import { Box, CircularProgress, IconButton, InputBase, Typography } from '@mui/material';
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
            ¿A donde vas?
          </Typography>
          <InputBase
            fullWidth
            inputProps={{ 'aria-label': 'Ubicacion' }}
            onChange={(event) => onLocationChange(event.target.value)}
            placeholder="Madrid, Spain"
            required
            sx={{ fontSize: 17, lineHeight: 1.2 }}
            value={location}
          />
        </Box>
      </Box>

      <Box sx={{ borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', borderRight: { md: 1 } }}>
        <DateTimePickerField
          helperText="Entrada"
          label="¿Cuando llegas?"
          onChange={onCheckInChange}
          value={checkIn}
        />
      </Box>
      <Box sx={{ borderBottom: { xs: 1, md: 0 }, borderColor: 'divider', borderRight: { md: 1 } }}>
        <DateTimePickerField
          helperText="Salida"
          label="¿Cuando te vas?"
          onChange={onCheckOutChange}
          value={checkOut}
        />
      </Box>
      <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'center', p: { xs: 2, md: 1.5 } }}>
        <IconButton
          aria-label="Buscar parking"
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
