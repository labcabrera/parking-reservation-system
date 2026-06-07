import { useEffect } from 'react';
import { userManager } from '../auth/oidc';

export default function SilentCallbackPage() {
  useEffect(() => {
    void userManager?.signinSilentCallback();
  }, []);

  return null;
}
