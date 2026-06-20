<template>
  <div class="text-center p-4">
    <p class="font-bold text-lg">Logging out...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useRuntimeConfig } from "#imports";
import { useAuth } from "~/composables/common/useAuth";

const router = useRouter();
const config = useRuntimeConfig();
const { clearLocalSession } = useAuth();

// Marks the round-trip back from Cognito's /logout so we don't redirect again.
const RETURN_FLAG = "post_logout_redirect";

const buildCognitoLogoutUrl = (): string | null => {
  const domain = config.public.cognitoDomain;
  const region = config.public.region;
  const clientId = config.public.cognitoClientId;
  if (!domain || !region || !clientId) {
    return null;
  }
  const url = new URL(
    `https://${domain}.auth.${region}.amazoncognito.com/logout`,
  );
  url.searchParams.set("client_id", String(clientId));
  // Must match an entry in the Cognito app client's "Allowed sign-out URLs"
  // (registered as the app's /logout route).
  url.searchParams.set("logout_uri", window.location.origin + "/logout");
  return url.toString();
};

onMounted(async () => {
  if (!import.meta.client) {
    return;
  }
  await clearLocalSession();

  const authDisabled = ["off", "false"].includes(
    config.public.requireAuth.toLowerCase(),
  );
  const logoutUrl = authDisabled ? null : buildCognitoLogoutUrl();

  // Returning from Cognito: session already cleared, send the user home where
  // the auth guard will trigger a fresh login.
  if (sessionStorage.getItem(RETURN_FLAG)) {
    sessionStorage.removeItem(RETURN_FLAG);
    router.replace("/");
    return;
  }

  if (logoutUrl) {
    sessionStorage.setItem(RETURN_FLAG, "1");
    window.location.href = logoutUrl;
  } else {
    router.replace("/");
  }
});
</script>
