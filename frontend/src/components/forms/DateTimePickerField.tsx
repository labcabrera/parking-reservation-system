import AccessTimeIcon from '@mui/icons-material/AccessTime';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import { Box, Button, ButtonBase, Divider, IconButton, Popover, Stack, Typography } from '@mui/material';
import { useMemo, useState } from 'react';

const monthFormatter = new Intl.DateTimeFormat('es-ES', { month: 'long', year: 'numeric' });
const dayFormatter = new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: 'short' });
const weekdays = ['LU', 'MA', 'MI', 'JU', 'VI', 'SA', 'DO'];
const timeSlots = Array.from({ length: 48 }, (_, index) => {
  const hours = String(Math.floor(index / 2)).padStart(2, '0');
  const minutes = index % 2 === 0 ? '00' : '30';

  return `${hours}:${minutes}`;
});

interface ParsedDateTime {
  date: Date;
  time: string;
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
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);
  const [displayMonth, setDisplayMonth] = useState(new Date(initialDate.getFullYear(), initialDate.getMonth(), 1));
  const [selectedDate, setSelectedDate] = useState<Date | null>(parsedValue?.date ?? null);
  const [selectedTime, setSelectedTime] = useState(parsedValue?.time ?? '10:00');
  const monthDays = useMemo(() => getMonthDays(displayMonth), [displayMonth]);
  const isOpen = Boolean(anchorEl);

  function handleOpen(event: React.MouseEvent<HTMLElement>) {
    const nextParsedValue = parseDateTimeValue(value);
    const nextDate = nextParsedValue?.date ?? new Date();

    setSelectedDate(nextDate);
    setSelectedTime(nextParsedValue?.time ?? '10:00');
    setDisplayMonth(new Date(nextDate.getFullYear(), nextDate.getMonth(), 1));
    setAnchorEl(event.currentTarget);
  }

  function handleSave() {
    onChange(buildDateTimeValue(selectedDate ?? new Date(), selectedTime));
    setAnchorEl(null);
  }

  return (
    <>
      <DateTimeTrigger
        helperText={helperText}
        isOpen={isOpen}
        label={getSelectedLabel(parsedValue, label)}
        onOpen={handleOpen}
      />
      <DateTimePopover anchorEl={anchorEl} isOpen={isOpen} onClose={() => setAnchorEl(null)}>
        <MonthNavigation displayMonth={displayMonth} onDisplayMonthChange={setDisplayMonth} />
        <CalendarGrid days={monthDays} onSelectDate={setSelectedDate} selectedDate={selectedDate} />
        <Divider />
        <TimeSlotList onSelectTime={setSelectedTime} selectedTime={selectedTime} />
        <Button fullWidth onClick={handleSave} size="large" variant="contained">
          Guardar
        </Button>
        <Typography align="center" color="text.secondary" variant="caption">
          La tarifa siempre se calcula por periodos completos.
        </Typography>
      </DateTimePopover>
    </>
  );
}

function DateTimeTrigger({
  helperText,
  isOpen,
  label,
  onOpen,
}: {
  helperText: string;
  isOpen: boolean;
  label: string;
  onOpen: (event: React.MouseEvent<HTMLElement>) => void;
}) {
  return (
    <ButtonBase
      aria-expanded={isOpen}
      aria-haspopup="dialog"
      onClick={onOpen}
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
          {label}
        </Typography>
      </Box>
      <KeyboardArrowDownIcon sx={{ color: 'text.secondary', ml: 'auto' }} />
    </ButtonBase>
  );
}

function DateTimePopover({
  anchorEl,
  children,
  isOpen,
  onClose,
}: {
  anchorEl: HTMLElement | null;
  children: React.ReactNode;
  isOpen: boolean;
  onClose: () => void;
}) {
  return (
    <Popover
      anchorEl={anchorEl}
      anchorOrigin={{ horizontal: 'center', vertical: 'bottom' }}
      onClose={onClose}
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
        {children}
      </Stack>
    </Popover>
  );
}

function MonthNavigation({
  displayMonth,
  onDisplayMonthChange,
}: {
  displayMonth: Date;
  onDisplayMonthChange: (date: Date) => void;
}) {
  function changeMonth(offset: number) {
    onDisplayMonthChange(new Date(displayMonth.getFullYear(), displayMonth.getMonth() + offset, 1));
  }

  return (
    <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
      <IconButton aria-label="Mes anterior" onClick={() => changeMonth(-1)} size="small">
        <ChevronLeftIcon />
      </IconButton>
      <Typography sx={{ fontWeight: 700, textTransform: 'capitalize' }}>
        {monthFormatter.format(displayMonth)}
      </Typography>
      <IconButton aria-label="Mes siguiente" onClick={() => changeMonth(1)} size="small">
        <ChevronRightIcon />
      </IconButton>
    </Stack>
  );
}

function CalendarGrid({
  days,
  onSelectDate,
  selectedDate,
}: {
  days: Array<Date | null>;
  onSelectDate: (date: Date) => void;
  selectedDate: Date | null;
}) {
  return (
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
      {days.map((day, index) =>
        day ? (
          <CalendarDay
            day={day}
            isSelected={isSameDay(day, selectedDate)}
            key={day.toISOString()}
            onSelect={onSelectDate}
          />
        ) : (
          <Box key={`empty-${index}`} />
        ),
      )}
    </Box>
  );
}

function CalendarDay({
  day,
  isSelected,
  onSelect,
}: {
  day: Date;
  isSelected: boolean;
  onSelect: (date: Date) => void;
}) {
  return (
    <ButtonBase
      aria-label={`Seleccionar ${day.toLocaleDateString('es-ES')}`}
      onClick={() => onSelect(day)}
      sx={{
        borderRadius: '50%',
        color: isSelected ? 'secondary.contrastText' : 'text.primary',
        height: 38,
        justifySelf: 'center',
        transition: 'background-color 120ms ease',
        width: 38,
        ...(isSelected && {
          bgcolor: 'secondary.main',
          fontWeight: 800,
        }),
        '&:hover': {
          bgcolor: isSelected ? 'secondary.dark' : 'action.hover',
        },
      }}
    >
      {day.getDate()}
    </ButtonBase>
  );
}

function TimeSlotList({
  onSelectTime,
  selectedTime,
}: {
  onSelectTime: (time: string) => void;
  selectedTime: string;
}) {
  return (
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
          <TimeSlot
            isSelected={selectedTime === time}
            key={time}
            onSelect={onSelectTime}
            time={time}
          />
        ))}
      </Box>
    </Stack>
  );
}

function TimeSlot({
  isSelected,
  onSelect,
  time,
}: {
  isSelected: boolean;
  onSelect: (time: string) => void;
  time: string;
}) {
  return (
    <ButtonBase
      onClick={() => onSelect(time)}
      sx={{
        color: isSelected ? 'secondary.main' : 'text.secondary',
        display: 'block',
        fontWeight: isSelected ? 800 : 500,
        py: 1.1,
        textAlign: 'center',
        width: '100%',
        ...(isSelected && { bgcolor: 'action.selected' }),
        '&:hover': { bgcolor: 'action.hover' },
      }}
    >
      {time}
    </ButtonBase>
  );
}

function getSelectedLabel(parsedValue: ParsedDateTime | null, fallbackLabel: string) {
  return parsedValue ? `${dayFormatter.format(parsedValue.date)} · ${parsedValue.time}` : fallbackLabel;
}

function toDateInputValue(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function buildDateTimeValue(date: Date, time: string) {
  return `${toDateInputValue(date)}T${time}`;
}

function parseDateTimeValue(value: string): ParsedDateTime | null {
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
