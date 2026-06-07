const AUTH_RETURN_TO_KEY = 'prs.auth.returnTo';

export function getSafeReturnTo(value: string | undefined | null, fallback = '/') {
  if (!value || !value.startsWith('/') || value.startsWith('/auth/')) {
    return fallback;
  }

  return value;
}

export function rememberCurrentRoute() {
  const returnTo = getSafeReturnTo(`${window.location.pathname}${window.location.search}${window.location.hash}`);
  window.sessionStorage.setItem(AUTH_RETURN_TO_KEY, returnTo);

  return returnTo;
}

export function consumeRememberedRoute(fallback = '/') {
  const returnTo = getSafeReturnTo(window.sessionStorage.getItem(AUTH_RETURN_TO_KEY), fallback);
  window.sessionStorage.removeItem(AUTH_RETURN_TO_KEY);

  return returnTo;
}
