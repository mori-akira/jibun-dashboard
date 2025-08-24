import { useAuth } from "~/composables/common/useAuth";

export default defineNuxtRouteMiddleware((to) => {
  const config = useRuntimeConfig();
  const requireAuth: boolean = config.public.requireAuth;

  if (!requireAuth) {
    return;
  }
  if (to.path.startsWith("/callback") || to.path.startsWith("/public")) {
    return;
  }

  const { getIdToken, isTokenExpired } = useAuth();
  const idToken = getIdToken();
  if (!idToken || isTokenExpired()) {
    localStorage.removeItem("id_token");
    localStorage.removeItem("access_token");
    const clientId = config.public.cognitoClientId;
    const region = config.public.region;
    const domain = config.public.cognitoDomain;
    const redirectUri = window.location.origin + "/callback";
    const loginUrl = `https://${domain}.auth.${region}.amazoncognito.com/login?response_type=token&client_id=${clientId}&redirect_uri=${redirectUri}`;
    console.log("Redirecting to login:", loginUrl);
    window.location.href = loginUrl;
  }
});
