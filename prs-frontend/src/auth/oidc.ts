import { UserManager, WebStorageStateStore, type UserManagerSettings } from 'oidc-client-ts';

const oidcAuthority = import.meta.env.VITE_OIDC_AUTHORITY as string | undefined;
const oidcClientId = import.meta.env.VITE_OIDC_CLIENT_ID as string | undefined;
const oidcScope = (import.meta.env.VITE_OIDC_SCOPE as string | undefined) ?? 'openid profile email';
const appOrigin = window.location.origin;

export const isOidcConfigured = Boolean(oidcAuthority && oidcClientId);

export const oidcSettings: UserManagerSettings | null = isOidcConfigured
  ? {
      authority: oidcAuthority as string,
      client_id: oidcClientId as string,
      redirect_uri:
        (import.meta.env.VITE_OIDC_REDIRECT_URI as string | undefined) ??
        `${appOrigin}/auth/callback`,
      post_logout_redirect_uri:
        (import.meta.env.VITE_OIDC_POST_LOGOUT_REDIRECT_URI as string | undefined) ??
        `${appOrigin}/auth/logout-callback`,
      response_type: 'code',
      scope: oidcScope,
      userStore: new WebStorageStateStore({ store: window.localStorage }),
    }
  : null;

export const userManager = oidcSettings ? new UserManager(oidcSettings) : null;
