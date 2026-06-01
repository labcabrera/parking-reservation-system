import AccessTimeIcon from '@mui/icons-material/AccessTime';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import {
  Box,
  Button,
  ButtonBase,
  Divider,
  IconButton,
  Popover,
  Stack,
  Typography,
} from '@mui/material';
import { useMemo, useState } from 'react';

const monthFormatter = new Intl.DateTimeFormat('es-ES', {
  month: 'long',
  year: 'numeric',
});

const dayFormatter = new Intl.DateTimeFormat('es-ES', {
  day: '2-digit',
  month: 'short',
});

const weekdays = ['LU', 'MA', 'MI', 'JU', 'VI', 'SA', 'DO'];
const timeSlots = ['08:00', '08:30', '09:00', '09:30', '10:00', '10:30', '11:00', '11:30', '12:00'];

function toDateInputValue(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function buildDateTimeValue(date: Date, time: string) {
  return `${toDateInputValue(date)}T${time}`;
}

function parseDateTimeValue(value: string) {
  if (!value) return null;

  const [datePart, timePart = '10:00'] = value.split('T');
  const [year, month, day] = datePart.split('-').map(Number);

  if (!year || !month || !day) return null;

  return {
    date: new Date(year, month - 1, day),
    time: timePart.slice(0, 5),
  };
}

function getMonthDays(displayMonth: Date) {
  const year = displayMonth.getFullYear();
  const month = displayMonth.getMonth();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDay = new Date(year, month, 1).getDay();
  const leadingEmptyDays = (firstDay + 6) % 7;

  return [
    ...Array.from({ length: leadingEmptyDays }, () => null),
    ...Array.from({ length: daysInMonth }, (_, index) => new Date(year, month, index + 1)),
  ];
}

function isSameDay(first: Date | null, second: Date | null) {
  return Boolean(
    first &&
      second &&
      first.getFullYear() === second.getFullYear() &&
      first.getMonth() === second.getMonth() &&
      first.getDate() === second.getDate(),
  );
}

interface DateTimePickerFieldProps {
  helperText: string;
  label: string;
  onChange: (value: string) => void;
  value: string;
}

export function DateTimePickerField({ helperText, label, onChange, value }: DateTimePickerFieldProps) {
  const parsedValue = parseDateTimeValue(value);
  const initialDate = parsedValue?.date ?? new Date();
  const initialTime = parsedValue?.time ?? '10:00';
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const [displayMonth, setDisplayMonth] = useState(new Date(initialDate.getFullYear(), initialDate.getMonth(), 1));
  const [selectedDate, setSelectedDate] = useState<Date | null>(parsedValue?.date ?? null);
  const [selectedTime, setSelectedTime] = useState(initialTime);
  const monthDays = useMemo(() => getMonthDays(displayMonth), [displayMonth]);
  const isOpen = Boolean(anchorEl);

  function handleOpen(event: React.MouseEvent<HTMLElement>) {
    const nextParsedValue = parseDateTimeValue(value);

    setSelectedDate(nextParsedValue?.date ?? new Date());
    setSelectedTime(nextParsedValue?.time ?? '10:00');
    setDisplayMonth(
      nextParsedValue
        ? new Date(nextParsedValue.date.getFullYear(), nextParsedValue.date.getMonth(), 1)
        : new Date(new Date().getFullYear(), new Date().getMonth(), 1),
    );
    setAnchorEl(event.currentTarget);
  }

  function handleSave() {
    onChange(buildDateTimeValue(selectedDate ?? new Date(), selectedTime));
    setAnchorEl(null);
  }

  const selectedLabel = parsedValue
    ? `${dayFormatter.format(parsedValue.date)} · ${parsedValue.time}`
    : label;

  return (
    <>
      <ButtonBase
        aria-haspopup="dialog"
        aria-expanded={isOpen}
        onClick={handleOpen}
        sx={{
          alignItems: 'center',
          display: 'flex',
          gap: 1.5,
          height: '100%',
          justifyContent: 'flex-start',
          px: { xs: 2.5, md: 3 },
          py: 2,
          textAlign: 'left',
          width: '100%',
        }}
      >
        <CalendarMonthIcon sx={{ color: 'text.secondary', fontSize: 20 }} />
        <Box sx={{ minWidth: 0 }}>
          <Typography color="text.secondary" variant="caption">
            {helperText}
          </Typography>
          <Typography sx={{ lineHeight: 1.2 }} variant="body1">
            {selectedLabel}
          </Typography>
        </Box>
        <KeyboardArrowDownIcon sx={{ color: 'text.secondary', ml: 'auto' }} />
      </ButtonBase>

      <Popover
        anchorEl={anchorEl}
        anchorOrigin={{ horizontal: 'center', vertical: 'bottom' }}
        onClose={() => setAnchorEl(null)}
        open={isOpen}
        slotProps={{
          paper: {
            sx: {
              borderRadius: 2,
              boxShadow: '0 22px 70px rgba(15, 23, 42, 0.24)',
              mt: 1.5,
              overflow: 'visible',
              p: 1.25,
              width: 360,
              '&::before': {
                bgcolor: 'background.paper',
                content: '""',
                height: 18,
                left: '50%',
                position: 'absolute',
                top: -8,
                transform: 'translateX(-50%) rotate(45deg)',
                width: 18,
              },
            },
          },
        }}
        transformOrigin={{ horizontal: 'center', vertical: 'top' }}
      >
        <Stack spacing={1.5} sx={{ position: 'relative' }}>
          <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
            <IconButton
              aria-label="Mes anterior"
              onClick={() => setDisplayMonth(new Date(displayMonth.getFullYear(), displayMonth.getMonth() - 1, 1))}
              size="small"
            >
              <ChevronLeftIcon />
            </IconButton>
            <Typography sx={{ fontWeight: 700, textTransform: 'capitalize' }}>
              {monthFormatter.format(displayMonth)}
            </Typography>
            <IconButton
              aria-label="Mes siguiente"
              onClick={() => setDisplayMonth(new Date(displayMonth.getFullYear(), displayMonth.getMonth() + 1, 1))}
              size="small"
            >
              <ChevronRightIcon />
            </IconButton>
          </Stack>

          <Box
            sx={{
              border: 1,
              borderColor: 'divider',
              borderRadius: 1.5,
              display: 'grid',
              gridTemplateColumns: 'repeat(7, 1fr)',
              p: 1,
              rowGap: 0.5,
            }}
          >
            {weekdays.map((day) => (
              <Typography
                key={day}
                align="center"
                color="text.secondary"
                sx={{ fontSize: 12, fontWeight: 700, py: 0.75 }}
              >
                {day}
              </Typography>
            ))}
            {monthDays.map((day, index) =>
              day ? (
                <ButtonBase
                  key={day.toISOString()}
                  aria-label={`Seleccionar ${day.toLocaleDateString('es-ES')}`}
                  onClick={() => setSelectedDate(day)}
                  sx={{
                    borderRadius: '50%',
                    color: isSameDay(day, selectedDate) ? 'secondary.contrastText' : 'text.primary',
                    height: 38,
                    justifySelf: 'center',
                    transition: 'background-color 120ms ease',
                    width: 38,
                    ...(isSameDay(day, selectedDate) && {
                      bgcolor: 'secondary.main',
                      fontWeight: 800,
                    }),
                    '&:hover': {
                      bgcolor: isSameDay(day, selectedDate) ? 'secondary.dark' : 'action.hover',
                    },
                  }}
                >
                  {day.getDate()}
                </ButtonBase>
              ) : (
                <Box key={`empty-${index}`} />
              ),
            )}
          </Box>

          <Divider />

          <Stack spacing={1}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <AccessTimeIcon sx={{ color: 'primary.main', fontSize: 18 }} />
              <Typography sx={{ fontWeight: 700 }}>¿A que hora llegas?</Typography>
            </Stack>
            <Box
              sx={{
                border: 1,
                borderColor: 'divider',
                borderRadius: 1.5,
                maxHeight: 118,
                overflowY: 'auto',
              }}
            >
              {timeSlots.map((time) => (
                <ButtonBase
                  key={time}
                  onClick={() => setSelectedTime(time)}
                  sx={{
                    color: selectedTime === time ? 'secondary.main' : 'text.secondary',
                    display: 'block',
                    fontWeight: selectedTime === time ? 800 : 500,
                    py: 1.1,
                    textAlign: 'center',
                    width: '100%',
                    ...(selectedTime === time && { bgcolor: 'action.selected' }),
                    '&:hover': { bgcolor: 'action.hover' },
                  }}
                >
                  {time}
                </ButtonBase>
              ))}
            </Box>
          </Stack>

          <Button fullWidth onClick={handleSave} size="large" variant="contained">
            Guardar
          </Button>
          <Typography align="center" color="text.secondary" variant="caption">
            La tarifa siempre se calcula por periodos completos.
          </Typography>
        </Stack>
      </Popover>
    </>
  );
}
