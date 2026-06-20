<template>
  <div class="text-center p-4">
    <p class="font-bold text-lg">Logging in...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useCommonStore } from "~/stores/common";
import { useAuth } from "~/composables/common/useAuth";

const commonStore = useCommonStore();
const { handleCallback } = useAuth();

onMounted(async () => {
  if (!import.meta.client) {
    return;
  }
  try {
    const returnPath = await handleCallback();
    await navigateTo(returnPath || "/");
  } catch (err) {
    commonStore.addErrorMessage("Failed to login. Please try again.");
    console.warn("Login failed:", err);
    await navigateTo("/");
  }
});
</script>
