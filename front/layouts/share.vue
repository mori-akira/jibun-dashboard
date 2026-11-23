<template>
  <div class="flex min-h-screen">
    <ShareHeader />
    <div class="w-screen flex bg-white">
      <ShareNavigation v-if="shareStatus === 'ok'" />
      <main
        class="flex-1 h-[calc(100vh-4rem)] mt-12 p-4 pr-8 overflow-y-scroll"
      >
        <NuxtPage />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import ShareHeader from "~/components/app/AppShareHeader.vue";
import ShareNavigation from "~/components/app/AppShareNavigation.vue";
import { AxiosError } from "axios";
import { useApiClient } from "~/composables/common/useApiClient";
import { useSettingStore } from "~/stores/setting";

const shareStatus = ref<"ok" | "gone">("ok");
provide("shareStatus", shareStatus);

// forbidden data types discovered across page navigations (not reset — stable per session)
const forbiddenTypes = ref<string[]>([]);
provide("forbiddenTypes", forbiddenTypes);

const route = useRoute();
watch(
  () => route.path,
  () => {
    shareStatus.value = "ok";
  },
);

// 共有元ユーザーの設定を settingStore に格納し、通常画面と同じコンポーネントで参照できるようにする
const { getShareApi } = useApiClient();
const settingStore = useSettingStore();
watch(
  () => route.params.token as string | undefined,
  async (token) => {
    settingStore.setting = null;
    if (!token) return;
    try {
      const res = await getShareApi().getShareSetting(token);
      settingStore.setting = res.data;
    } catch (err) {
      if (err instanceof AxiosError && err.response?.status === 410) {
        shareStatus.value = "gone";
      }
    }
  },
  { immediate: true },
);
</script>
