import {
  UserManager,
  WebStorageStateStore,
  type User,
  type UserManagerSettings,
} from "oidc-client-ts";

// Single UserManager instance shared across the client app (SSR is disabled).
let manager: UserManager | null = null;
// Synchronous cache of the current access token so callers (useApiClient,
// i18n.api) can read it without awaiting storage. Kept fresh via UserManager
// events and the loadUser/handleCallback flows below.
let cachedAccessToken: string | null = null;

const isAuthDisabled = (requireAuth: string): boolean =>
  ["off", "false"].includes(requireAuth.toLowerCase());

const getManager = (): UserManager => {
  if (manager) {
    return manager;
  }
  const config = useRuntimeConfig();
  const region = config.public.region as string;
  const userPoolId = config.public.cognitoUserPoolId as string;
  const clientId = config.public.cognitoClientId as string;

  const settings: UserManagerSettings = {
    authority: `https://cognito-idp.${region}.amazonaws.com/${userPoolId}`,
    client_id: clientId,
    // Trailing slash is required: the S3 website host 302-redirects
    // "/callback" -> "/callback/" and drops the query string (code/state)
    // in the process, which breaks the Authorization Code response parsing.
    redirect_uri: `${window.location.origin}/callback/`,
    response_type: "code",
    scope: "openid email profile aws.cognito.signin.user.admin",
    userStore: new WebStorageStateStore({ store: window.localStorage }),
    stateStore: new WebStorageStateStore({ store: window.localStorage }),
    automaticSilentRenew: true,
    monitorSession: false,
    // email is available in the id_token; skip the extra userinfo round-trip.
    loadUserInfo: false,
  };

  manager = new UserManager(settings);
  manager.events.addUserLoaded((user) => {
    cachedAccessToken = user.access_token ?? null;
  });
  manager.events.addUserUnloaded(() => {
    cachedAccessToken = null;
  });
  manager.events.addSilentRenewError((err) => {
    if (import.meta.dev) {
      console.error("Silent token renew failed:", err);
    }
  });
  return manager;
};

export const useAuth = () => {
  const config = useRuntimeConfig();
  const authDisabled = isAuthDisabled(config.public.requireAuth as string);

  // Returns a valid (non-expired) user, transparently renewing via the refresh
  // token when the stored access token has expired. Primes the token cache.
  const loadUser = async (): Promise<User | null> => {
    if (authDisabled || !import.meta.client) {
      return null;
    }
    const mgr = getManager();
    let user = await mgr.getUser();
    if (user && user.expired) {
      user = await mgr.signinSilent().catch(() => null);
    }
    const valid = user && !user.expired ? user : null;
    cachedAccessToken = valid?.access_token ?? null;
    return valid;
  };

  const isAuthenticated = async (): Promise<boolean> => {
    if (authDisabled) {
      return true;
    }
    return (await loadUser()) !== null;
  };

  const getAccessToken = (): string | null => cachedAccessToken;

  // Starts the Authorization Code + PKCE login (full redirect to Cognito).
  const login = async (returnPath?: string): Promise<void> => {
    if (authDisabled || !import.meta.client) {
      return;
    }
    await getManager().signinRedirect({ state: returnPath ?? "/" });
  };

  // Completes the login on the /callback route; returns the path to resume.
  const handleCallback = async (): Promise<string> => {
    const user = await getManager().signinRedirectCallback();
    cachedAccessToken = user.access_token ?? null;
    return (user.state as string) || "/";
  };

  // Clears the locally stored tokens (does not redirect to Cognito).
  const clearLocalSession = async (): Promise<void> => {
    if (!import.meta.client) {
      return;
    }
    await getManager().removeUser();
    cachedAccessToken = null;
  };

  return {
    loadUser,
    isAuthenticated,
    getAccessToken,
    login,
    handleCallback,
    clearLocalSession,
  };
};
