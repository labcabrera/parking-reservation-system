import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { CssBaseline, ThemeProvider } from '@mui/material'
import { AuthProvider } from './auth/AuthContext'
import { RequireAuth } from './auth/RequireAuth'
import { AppLayout } from './components/layout/AppLayout'
import { theme } from './theme'
import AdminHomePage from './pages/admin/AdminHomePage'
import AdminOperationsConsolePage from './pages/admin/AdminOperationsConsolePage'
import AdminPricingPage from './pages/admin/AdminPricingPage'
import AuthCallbackPage from './pages/AuthCallbackPage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import LogoutCallbackPage from './pages/LogoutCallbackPage'
import SilentCallbackPage from './pages/SilentCallbackPage'
import SearchPage from './pages/SearchPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
})

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthProvider>
            <AppLayout>
              <Routes>
                <Route path="/" element={<SearchPage />} />
                <Route path="/admin" element={<RequireAuth><AdminHomePage /></RequireAuth>} />
                <Route path="/admin/facilities" element={<RequireAuth><AdminDashboardPage /></RequireAuth>} />
                <Route path="/admin/pricing" element={<RequireAuth><AdminPricingPage /></RequireAuth>} />
                <Route path="/admin/operations" element={<RequireAuth><AdminOperationsConsolePage /></RequireAuth>} />
                <Route path="/auth/callback" element={<AuthCallbackPage />} />
                <Route path="/auth/logout-callback" element={<LogoutCallbackPage />} />
                <Route path="/auth/silent-callback" element={<SilentCallbackPage />} />
              </Routes>
            </AppLayout>
          </AuthProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </ThemeProvider>
  )
}

export default App
