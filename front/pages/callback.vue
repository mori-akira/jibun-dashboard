<template>
  <div class="text-center p-4">
    <p class="font-bold text-lg">Logging in...</p>
  </div>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { useSettingStore } from "~/stores/setting";

const commonStore = useCommonStore();
const userStore = useUserStore();
const settingStore = useSettingStore();
const router = useRoute();

const extractTokensFromHash = (): Record<string, string> => {
  const hash = router?.hash?.substring(1) ?? "";
  return Object.fromEntries(new URLSearchParams(hash));
};
const tokens = extractTokensFromHash();

if (tokens.id_token && import.meta.client) {
  localStorage.setItem("id_token", tokens.id_token);
  localStorage.setItem("access_token", tokens.access_token ?? "");
  await withErrorHandling(
    async () =>
      await Promise.all([userStore.fetchUser(), settingStore.fetchSetting()]),
    commonStore
  );
  navigateTo("/");
} else {
  commonStore.addErrorMessage("Failed to login. Please try again.");
  console.warn("Login failed:", tokens);
  navigateTo("/");
}
</script>
