import axios from "axios";
import { useAuth } from "~/composables/common/useAuth";

export default defineNuxtRouteMiddleware(() => {
  const config = useRuntimeConfig();
  const requireAuth: boolean = config.public.requireAuth;

  if (!requireAuth) {
    return;
  }

  const { getIdToken, getAccessToken, isTokenExpired } = useAuth();
  const idToken = getIdToken();
  if (!idToken || isTokenExpired()) {
    localStorage.clear();
    const clientId = config.public.cognitoClientId;
    const region = config.public.region;
    const domain = config.public.cognitoDomain;
    const redirectUri = window.location.origin + "/callback";
    const loginUrl = `https://${domain}.auth.${region}.amazoncognito.com/login?response_type=token&client_id=${clientId}&redirect_uri=${redirectUri}`;
    window.location.href = loginUrl;
  }

  const accessToken = getAccessToken();
  axios.interceptors.request.use((req) => {
    req.headers.Authorization = `Bearer ${accessToken}`;
    return req;
  });
});
