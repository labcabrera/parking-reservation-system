import { userManager } from '../auth/oidc';

export function getBaseUrl(envName: string, fallback = '') {
  const value = import.meta.env[envName] as string | undefined;
  return value?.replace(/\/$/, '') ?? fallback;
}

export async function authenticatedFetch(input: RequestInfo | URL, init: RequestInit = {}) {
  const storedUser = await userManager?.getUser();
  const user = storedUser?.expired ? await userManager?.signinSilent().catch(() => null) : storedUser;
  const token = user && !user.expired ? user.access_token : undefined;
  const headers = new Headers(init.headers);

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  return fetch(input, {
    ...init,
    headers,
  });
}

export async function parseJsonResponse<T>(response: Response, message: string): Promise<T> {
  const contentType = response.headers.get('content-type') ?? '';

  if (!response.ok) {
    throw new Error(`${message}: ${response.status} ${response.statusText}`);
  }

  if (!contentType.includes('application/json')) {
    throw new Error(`${message}: expected JSON response but received ${contentType || 'unknown content type'}`);
  }

  return response.json() as Promise<T>;
}

export async function parseOptionalJsonResponse<T>(response: Response, message: string): Promise<T> {
  if (!response.ok) {
    throw new Error(`${message}: ${response.status} ${response.statusText}`);
  }

  const contentType = response.headers.get('content-type') ?? '';
  if (!contentType.includes('application/json')) {
    return {} as T;
  }

  return response.json() as Promise<T>;
}
