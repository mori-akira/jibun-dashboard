import { useAuth } from "~/composables/common/useAuth";

export default defineNuxtRouteMiddleware(async (to) => {
  if (!import.meta.client) {
    return;
  }
  const config = useRuntimeConfig();
  const requireAuth = config.public.requireAuth.toLowerCase();

  if (["off", "false"].includes(requireAuth)) {
    return;
  }
  if (to.path.startsWith("/callback") || to.path.startsWith("/public") || to.path.startsWith("/share/")) {
    return;
  }

  const { isAuthenticated, login } = useAuth();
  if (!(await isAuthenticated())) {
    await login(to.fullPath);
    return abortNavigation();
  }
});
