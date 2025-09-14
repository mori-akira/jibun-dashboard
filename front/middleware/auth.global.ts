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
    const loginUrl = new URL(
      `https://${domain}.auth.${region}.amazoncognito.com/login`
    );
    loginUrl.searchParams.set("response_type", "token");
    loginUrl.searchParams.set("client_id", String(clientId));
    loginUrl.searchParams.set(
      "redirect_uri",
      window.location.origin + "/callback"
    );
    window.location.href = loginUrl.toString();
  }
});
