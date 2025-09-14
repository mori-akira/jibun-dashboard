<template>
  <div class="text-center p-4">
    <p class="font-bold text-lg">Logging out...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useRuntimeConfig } from "#imports";

const router = useRouter();
const config = useRuntimeConfig();

const logoutUrl = ref<string | null>(null);

const buildCognitoLogoutUrl = (): string | null => {
  const requireAuth = !!config.public.requireAuth;
  const domain = config.public.cognitoDomain;
  const region = config.public.region;
  const clientId = config.public.cognitoClientId;

  if (!requireAuth) {
    return null;
  }
  if (!domain || !region || !clientId) {
    return null;
  }

  const url = new URL(
    `https://${domain}.auth.${region}.amazoncognito.com/logout`
  );
  url.searchParams.set("response_type", "token");
  url.searchParams.set("client_id", String(clientId));
  url.searchParams.set("redirect_uri", window.location.origin + "/callback");
  return url.toString();
};

const clearLocalAuth = () => {
  if (!import.meta.client) return;
  try {
    localStorage.removeItem("id_token");
    localStorage.removeItem("access_token");
  } catch {
    // noop
  }
};

onMounted(() => {
  clearLocalAuth();
  logoutUrl.value = buildCognitoLogoutUrl();
  if (logoutUrl.value && import.meta.client) {
    window.location.href = logoutUrl.value;
  } else {
    router.replace("/");
  }
});
</script>
