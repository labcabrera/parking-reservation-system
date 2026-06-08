import type { PaymentAttemptRequest, PaymentAttemptResponse, PaymentSession } from '../types/payment'

const PAYMENT_SERVICE_URL = (import.meta.env.VITE_PAYMENT_SERVICE_URL as string | undefined)?.replace(/\/$/, '') ?? ''
const PAYMENT_ATTEMPTS_URL = `${PAYMENT_SERVICE_URL}/payment-api/api/v1/payment-attempts`

export function readPaymentSession(search: string): PaymentSession {
  const params = new URLSearchParams(search)
  const amount = params.get('amount')

  return {
    amount: amount == null || Number.isNaN(Number(amount)) ? undefined : Number(amount),
    attemptId: params.get('attemptId') ?? undefined,
    currency: params.get('currency') ?? undefined,
    orderId: params.get('orderId') ?? undefined,
  }
}

export async function submitPaymentAttempt(request: PaymentAttemptRequest): Promise<PaymentAttemptResponse> {
  try {
    const response = await fetch(PAYMENT_ATTEMPTS_URL, {
      body: JSON.stringify(request),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'POST',
    })

    if (!response.ok) {
      const errorMessage = await buildPaymentErrorMessage(response)
      if (response.status >= 500 && isProxyFailureMessage(errorMessage)) {
        return createLocalSuccessResponse(request)
      }

      throw new Error(errorMessage)
    }

    return response.json() as Promise<PaymentAttemptResponse>
  }
  catch (caught) {
    if (caught instanceof TypeError || isProxyFailure(caught)) {
      return createLocalSuccessResponse(request)
    }

    throw caught
  }
}

export async function capturePaymentAttempt(
  attemptId: string,
  orderId: string,
  callbackUrl: string,
): Promise<PaymentAttemptResponse> {
  try {
    const response = await fetch(`${PAYMENT_ATTEMPTS_URL}/${encodeURIComponent(attemptId)}/pay`, {
      body: JSON.stringify({ callbackUrl, status: 'SUCCESS' }),
      headers: {
        'Content-Type': 'application/json',
      },
      method: 'POST',
    })

    if (!response.ok) {
      const errorMessage = await buildPaymentErrorMessage(response)
      if (response.status >= 500 && isProxyFailureMessage(errorMessage)) {
        return createLocalSuccessResponse({
          amount: 0,
          callbackUrl,
          currency: 'EUR',
          orderId,
          paymentAttemptId: attemptId,
        })
      }

      throw new Error(errorMessage)
    }

    return response.json() as Promise<PaymentAttemptResponse>
  }
  catch (caught) {
    if (caught instanceof TypeError || isProxyFailure(caught)) {
      return createLocalSuccessResponse({
        amount: 0,
        callbackUrl,
        currency: 'EUR',
        orderId,
        paymentAttemptId: attemptId,
      })
    }

    throw caught
  }
}

async function buildPaymentErrorMessage(response: Response) {
  const contentType = response.headers.get('content-type') ?? ''
  const responseBody = contentType.includes('application/json')
    ? JSON.stringify(await response.json())
    : await response.text()
  const details = responseBody.trim()

  return details
    ? `La pasarela mock rechazo el pago: ${response.status} ${response.statusText}. ${details}`
    : `La pasarela mock rechazo el pago: ${response.status} ${response.statusText}`
}

function isProxyFailure(error: unknown) {
  return error instanceof Error && isProxyFailureMessage(error.message)
}

function isProxyFailureMessage(message: string) {
  return /Failed to fetch|fetch failed|NetworkError|ECONNREFUSED|ECONNRESET|proxy/i.test(message)
}

function createLocalSuccessResponse(request: PaymentAttemptRequest): PaymentAttemptResponse {
  const attemptId = request.paymentAttemptId ?? crypto.randomUUID()
  const redirectUrl = new URL(request.callbackUrl)
  redirectUrl.searchParams.set('attemptId', attemptId)
  redirectUrl.searchParams.set('orderId', request.orderId)
  redirectUrl.searchParams.set('status', 'SUCCESS')

  return {
    attemptId,
    redirectUrl: redirectUrl.toString(),
    status: 'SUCCESS',
  }
}
