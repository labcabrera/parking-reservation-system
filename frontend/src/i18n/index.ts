import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

export const supportedLanguages = [
  { code: 'es', label: 'ESP' },
  { code: 'en', label: 'ENG' },
] as const;

export type SupportedLanguage = (typeof supportedLanguages)[number]['code'];

const resources = {
  es: {
    translation: {
      auth: {
        login: 'Entrar',
        logout: 'Salir',
        loginTooltip: 'Iniciar sesion',
        missingConfig: 'Configura VITE_OIDC_AUTHORITY y VITE_OIDC_CLIENT_ID',
        defaultUser: 'Usuario',
        callback: {
          login: 'Completando inicio de sesion...',
          logout: 'Cerrando sesion...',
          loginError: 'No se pudo completar el login',
          logoutError: 'No se pudo completar el logout',
        },
      },
      common: {
        search: 'Buscar parking',
      },
      footer: {
        search: 'Busqueda',
        product: 'Parking Reservation System',
      },
      header: {
        brand: 'Parking Demo Service',
        subtitle: 'Mobility services',
        nav: {
          bookings: 'Reservas',
          business: 'Empresas',
          parkings: 'Parkings',
          support: 'Soporte',
        },
      },
      search: {
        empty: 'No se encontraron parkings para la busqueda.',
        error: 'No se pudieron cargar los resultados',
        form: {
          checkInHelp: 'Entrada',
          checkInLabel: '¿Cuando llegas?',
          checkOutHelp: 'Salida',
          checkOutLabel: '¿Cuando te vas?',
          locationAria: 'Ubicacion',
          locationHelp: '¿A donde vas?',
          locationPlaceholder: 'Madrid, Spain',
        },
        hero: {
          subtitle: 'Aparcamientos urbanos, reservas para empresas y control de disponibilidad en tiempo real.',
          title: 'Reserva justo la plaza que necesitas',
        },
      },
      results: {
        benefits: {
          courtesy: '2h de cortesia',
          express: 'Entrada express',
          freeCancellation: 'Cancelacion gratuita',
          unlimited: 'Entradas y salidas ilimitadas',
        },
        choose: 'Elige este parking',
        dayPrice: '{{price}}/dia',
        lowAvailability: 'Quedan pocas plazas para este parking',
        reserveHold: 'Te reservamos estas opciones durante {{time}} minutos.',
        spots: '{{count}} plazas',
        title: '{{count}} parkings cerca de {{location}}',
        totalPrice: '{{price}} total',
        sortRecommended: 'Ordenar: Recomendados',
      },
      dateTime: {
        nextMonth: 'Mes siguiente',
        previousMonth: 'Mes anterior',
        save: 'Guardar',
        selectDate: 'Seleccionar {{date}}',
        tariffNote: 'La tarifa siempre se calcula por periodos completos.',
        timePrompt: '¿A que hora llegas?',
        weekdays: ['LU', 'MA', 'MI', 'JU', 'VI', 'SA', 'DO'],
      },
    },
  },
  en: {
    translation: {
      auth: {
        login: 'Sign in',
        logout: 'Sign out',
        loginTooltip: 'Sign in',
        missingConfig: 'Configure VITE_OIDC_AUTHORITY and VITE_OIDC_CLIENT_ID',
        defaultUser: 'User',
        callback: {
          login: 'Completing sign in...',
          logout: 'Signing out...',
          loginError: 'Sign in could not be completed',
          logoutError: 'Sign out could not be completed',
        },
      },
      common: {
        search: 'Search parking',
      },
      footer: {
        search: 'Search',
        product: 'Parking Reservation System',
      },
      header: {
        brand: 'Parking Demo Service',
        subtitle: 'Mobility services',
        nav: {
          bookings: 'Bookings',
          business: 'Business',
          parkings: 'Parkings',
          support: 'Support',
        },
      },
      search: {
        empty: 'No parking facilities found for this search.',
        error: 'Results could not be loaded',
        form: {
          checkInHelp: 'Arrival',
          checkInLabel: 'When do you arrive?',
          checkOutHelp: 'Departure',
          checkOutLabel: 'When do you leave?',
          locationAria: 'Location',
          locationHelp: 'Where are you going?',
          locationPlaceholder: 'Madrid, Spain',
        },
        hero: {
          subtitle: 'Urban parking, business bookings and real-time availability control.',
          title: 'Book exactly the parking space you need',
        },
      },
      results: {
        benefits: {
          courtesy: '2h courtesy',
          express: 'Express entry',
          freeCancellation: 'Free cancellation',
          unlimited: 'Unlimited entries and exits',
        },
        choose: 'Choose this parking',
        dayPrice: '{{price}}/day',
        lowAvailability: 'Few spaces left for this parking',
        reserveHold: 'We will hold these options for {{time}} minutes.',
        spots: '{{count}} spaces',
        title: '{{count}} parkings near {{location}}',
        totalPrice: '{{price}} total',
        sortRecommended: 'Sort: Recommended',
      },
      dateTime: {
        nextMonth: 'Next month',
        previousMonth: 'Previous month',
        save: 'Save',
        selectDate: 'Select {{date}}',
        tariffNote: 'The rate is always calculated in complete periods.',
        timePrompt: 'What time do you arrive?',
        weekdays: ['MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU'],
      },
    },
  },
};

void i18n.use(initReactI18next).init({
  fallbackLng: 'es',
  interpolation: {
    escapeValue: false,
  },
  lng: window.localStorage.getItem('language') ?? 'es',
  resources,
  supportedLngs: supportedLanguages.map(({ code }) => code),
});

export { i18n };
