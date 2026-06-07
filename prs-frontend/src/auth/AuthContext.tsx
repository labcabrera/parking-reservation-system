import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type { User } from 'oidc-client-ts';
import { isOidcConfigured, userManager } from './oidc';
import { rememberCurrentRoute } from './authRedirect';

interface AuthContextValue {
  isAuthenticated: boolean;
  isConfigured: boolean;
  isLoading: boolean;
  user: User | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  completeLogin: () => Promise<User>;
  completeLogout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(isOidcConfigured);

  useEffect(() => {
    const manager = userManager;

    if (!manager) {
      setIsLoading(false);
      return undefined;
    }

    let isMounted = true;

    manager
      .getUser()
      .then((currentUser) => {
        if (isMounted) {
          setUser(currentUser && !currentUser.expired ? currentUser : null);
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsLoading(false);
        }
      });

    const handleUserLoaded = (loadedUser: User) => setUser(loadedUser);
    const handleUserUnloaded = () => setUser(null);
    const handleAccessTokenExpired = () => setUser(null);

    manager.events.addUserLoaded(handleUserLoaded);
    manager.events.addUserUnloaded(handleUserUnloaded);
    manager.events.addAccessTokenExpired(handleAccessTokenExpired);

    return () => {
      isMounted = false;
      manager.events.removeUserLoaded(handleUserLoaded);
      manager.events.removeUserUnloaded(handleUserUnloaded);
      manager.events.removeAccessTokenExpired(handleAccessTokenExpired);
    };
  }, []);

  const login = useCallback(async () => {
    if (!userManager) {
      throw new Error('OIDC is not configured');
    }

    await userManager.clearStaleState().catch(() => undefined);

    const returnTo = rememberCurrentRoute();
    await userManager.signinRedirect({
      state: {
        returnTo,
      },
    });
  }, []);

  const logout = useCallback(async () => {
    if (!userManager) {
      throw new Error('OIDC is not configured');
    }

    const currentUser = user ?? (await userManager.getUser());
    if (!currentUser) {
      setUser(null);
      return;
    }

    await userManager.signoutRedirect();
  }, [user]);

  const completeLogin = useCallback(async () => {
    if (!userManager) {
      throw new Error('OIDC is not configured');
    }

    const loggedUser = await userManager.signinRedirectCallback();
    setUser(loggedUser);
    return loggedUser;
  }, []);

  const completeLogout = useCallback(async () => {
    if (!userManager) {
      throw new Error('OIDC is not configured');
    }

    await userManager.signoutRedirectCallback();
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: Boolean(user && !user.expired),
      isConfigured: isOidcConfigured,
      isLoading,
      user,
      login,
      logout,
      completeLogin,
      completeLogout,
    }),
    [completeLogin, completeLogout, isLoading, login, logout, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}
