<template>
  <div class="text-center p-4">
    <p class="font-bold text-lg">Logging in...</p>
  </div>
</template>

<script setup lang="ts">
import { useCommonStore } from "~/stores/common";

const commonStore = useCommonStore();

const extractTokensFromHash = (): Record<string, string> => {
  const hash = window.location.hash.substring(1);
  return Object.fromEntries(new URLSearchParams(hash));
};
const tokens = extractTokensFromHash();

if (tokens.id_token) {
  localStorage.setItem("id_token", tokens.id_token);
  localStorage.setItem("access_token", tokens.access_token);
  navigateTo("/");
} else {
  commonStore.addErrorMessage("Failed to login. Please try again.");
  navigateTo("/");
}
</script>
