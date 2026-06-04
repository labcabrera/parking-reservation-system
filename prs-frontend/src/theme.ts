import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#111827',
      dark: '#070b12',
      light: '#374151',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#ff7900',
      dark: '#d65f00',
      light: '#ff9a35',
      contrastText: '#ffffff',
    },
    background: {
      default: '#f4f5f7',
      paper: '#ffffff',
    },
  },
  shape: {
    borderRadius: 8,
  },
  typography: {
    fontFamily:
      '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    h1: {
      fontSize: '2.25rem',
      fontWeight: 700,
    },
    h2: {
      fontSize: '1.75rem',
      fontWeight: 700,
    },
    button: {
      fontWeight: 700,
      textTransform: 'none',
    },
  },
  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: '1px solid rgba(17, 24, 39, 0.10)',
          boxShadow: '0 16px 42px rgba(17, 24, 39, 0.08)',
        },
      },
    },
  },
});
