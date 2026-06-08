import { FormEvent, useMemo, useState } from 'react'
import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { capturePaymentAttempt, readPaymentSession, submitPaymentAttempt } from './services/paymentGatewayApi'
import type { CardFormState, PaymentAttemptResponse, PaymentSession } from './types/payment'

const initialCardForm: CardFormState = {
  cardholderName: 'Alex Parking',
  cardNumber: '4242 4242 4242 4242',
  cvc: '123',
  expiry: '12/30',
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/payment" element={<PaymentPage />} />
        <Route path="/payment-result" element={<PaymentResultPage />} />
        <Route path="/" element={<PaymentPage />} />
      </Routes>
    </BrowserRouter>
  )
}

function PaymentPage() {
  const location = useLocation()
  const session = useMemo(() => readPaymentSession(location.search), [location.search])
  const [form, setForm] = useState<CardFormState>(initialCardForm)
  const [result, setResult] = useState<PaymentAttemptResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [status, setStatus] = useState<'idle' | 'paying' | 'cancelled'>('idle')
  const canSubmit = hasRegisteredPaymentAttempt(session) || hasLegacyPaymentData(session)
    ? status !== 'paying'
    : false

  function updateForm(field: keyof CardFormState, value: string) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!hasRegisteredPaymentAttempt(session) && !hasLegacyPaymentData(session)) {
      setError('Faltan datos del pago en la URL.')
      return
    }

    setStatus('paying')
    setError(null)

    try {
      const callbackUrl = `${window.location.origin}/payment-result`
      const response = hasRegisteredPaymentAttempt(session)
        ? await capturePaymentAttempt(session.attemptId, session.orderId, callbackUrl)
        : await submitPaymentAttempt({
            amount: session.amount,
            callbackUrl,
            currency: session.currency,
            orderId: session.orderId,
          })
      setResult(response)
      if (response.redirectUrl) {
        window.location.assign(response.redirectUrl)
      }
    }
    catch (caught) {
      setStatus('idle')
      setError(caught instanceof Error ? caught.message : 'No se pudo procesar el pago.')
    }
  }

  function handleCancel() {
    setStatus('cancelled')
    setResult(null)
    setError(null)
  }

  return (
    <main className="gateway-shell">
      <section className="gateway-card" aria-labelledby="payment-title">
        <PaymentHeader />

        {!hasRegisteredPaymentAttempt(session) && !hasLegacyPaymentData(session) && (
          <div className="alert alert-error" role="alert">
            No se puede iniciar el pago porque faltan orderId e attemptId en la URL.
          </div>
        )}

        {status === 'cancelled' && (
          <div className="alert alert-warning" role="status">
            Pago cancelado. Puedes cerrar esta ventana o volver al comercio.
          </div>
        )}

        {error && (
          <div className="alert alert-error" role="alert">
            {error}
          </div>
        )}

        {result && (
          <div className="alert alert-success" role="status">
            Pago aceptado. Redirigiendo al resultado...
          </div>
        )}

        <div className="gateway-layout">
          <PaymentSummary session={session} />
          <form className="payment-form" onSubmit={handleSubmit}>
            <div>
              <h2>Datos de la tarjeta</h2>
              <p className="muted">Formulario de simulacion para entorno de desarrollo.</p>
            </div>

            <label>
              Titular
              <input
                autoComplete="cc-name"
                onChange={(event) => updateForm('cardholderName', event.target.value)}
                required
                type="text"
                value={form.cardholderName}
              />
            </label>

            <label>
              Numero de tarjeta
              <input
                autoComplete="cc-number"
                inputMode="numeric"
                onChange={(event) => updateForm('cardNumber', event.target.value)}
                required
                type="text"
                value={form.cardNumber}
              />
            </label>

            <div className="form-grid">
              <label>
                Caducidad
                <input
                  autoComplete="cc-exp"
                  inputMode="numeric"
                  onChange={(event) => updateForm('expiry', event.target.value)}
                  required
                  type="text"
                  value={form.expiry}
                />
              </label>

              <label>
                CVC
                <input
                  autoComplete="cc-csc"
                  inputMode="numeric"
                  maxLength={4}
                  onChange={(event) => updateForm('cvc', event.target.value)}
                  required
                  type="password"
                  value={form.cvc}
                />
              </label>
            </div>

            <div className="button-row">
              <button className="button button-secondary" onClick={handleCancel} type="button">
                Cancelar
              </button>
              <button className="button button-primary" disabled={!canSubmit} type="submit">
                {status === 'paying' ? 'Procesando...' : 'Pagar'}
              </button>
            </div>
          </form>
        </div>
      </section>
    </main>
  )
}

function PaymentHeader() {
  return (
    <header className="gateway-header">
      <div>
        <p className="eyebrow">Secure mock gateway</p>
        <h1 id="payment-title">Confirmar pago</h1>
      </div>
      <div className="gateway-badge">PRS Pay</div>
    </header>
  )
}

function PaymentSummary({ session }: { session: PaymentSession }) {
  return (
    <aside className="summary-panel" aria-label="Resumen del pago">
      <h2>Resumen</h2>
      <SummaryRow label="Comercio" value="Parking Reservation System" />
      <SummaryRow label="Pedido" value={session.orderId ?? '-'} />
      <SummaryRow label="Intento" value={session.attemptId ?? '-'} />
      <div className="total-row">
        <span>Total</span>
        <strong>{formatMoney(session.amount, session.currency)}</strong>
      </div>
      <p className="muted">
        Esta pantalla simula una pasarela externa. No se envian datos reales de tarjeta.
      </p>
    </aside>
  )
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="summary-row">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  )
}

function PaymentResultPage() {
  const location = useLocation()
  const params = new URLSearchParams(location.search)
  const status = params.get('status') ?? 'UNKNOWN'
  const orderId = params.get('orderId') ?? '-'
  const attemptId = params.get('attemptId') ?? '-'
  const isSuccess = status === 'SUCCESS'
  const frontendUrl = buildFrontendReturnUrl(status, orderId, attemptId)

  return (
    <main className="gateway-shell">
      <section className="gateway-card result-card" aria-labelledby="result-title">
        <p className={isSuccess ? 'result-icon success' : 'result-icon failed'}>{isSuccess ? 'OK' : '!'}</p>
        <h1 id="result-title">{isSuccess ? 'Pago autorizado' : 'Pago no completado'}</h1>
        <p className="muted">
          {isSuccess
            ? 'El pago se ha comunicado correctamente al comercio. Ya puedes volver a Parking Reservation System.'
            : `Estado recibido: ${status}`}
        </p>
        <div className="result-details">
          <SummaryRow label="Pedido" value={orderId} />
          <SummaryRow label="Intento" value={attemptId} />
        </div>
        <a className="button button-primary" href={frontendUrl}>
          Volver al frontal
        </a>
      </section>
    </main>
  )
}

function hasRegisteredPaymentAttempt(session: PaymentSession): session is Required<Pick<PaymentSession, 'attemptId' | 'orderId'>> & PaymentSession {
  return Boolean(session.orderId && session.attemptId)
}

function hasLegacyPaymentData(session: PaymentSession): session is Required<Pick<PaymentSession, 'amount' | 'currency' | 'orderId'>> & PaymentSession {
  return Boolean(session.orderId && session.currency && session.amount != null)
}

function formatMoney(amount: number | undefined, currency: string | undefined) {
  if (amount == null || !currency) {
    return '-'
  }

  return new Intl.NumberFormat('es-ES', {
    currency,
    style: 'currency',
  }).format(amount)
}

function buildFrontendReturnUrl(status: string, orderId: string, attemptId: string) {
  const frontendBaseUrl = (import.meta.env.VITE_FRONTEND_URL as string | undefined) ?? 'http://localhost:3000'
  const returnUrl = new URL(frontendBaseUrl)
  returnUrl.searchParams.set('paymentStatus', status)
  if (orderId !== '-') {
    returnUrl.searchParams.set('orderId', orderId)
  }
  if (attemptId !== '-') {
    returnUrl.searchParams.set('attemptId', attemptId)
  }
  return returnUrl.toString()
}

export default App
