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
import type { SharedLinkBaseDataTypesEnum } from "~/generated/api/client";
import { useApiClient } from "~/composables/common/useApiClient";
import { useSettingStore } from "~/stores/setting";

const shareStatus = ref<"ok" | "gone">("ok");
provide("shareStatus", shareStatus);

// 共有リンクで閲覧可能なデータ種別（取得前は null）
const sharedDataTypes = ref<SharedLinkBaseDataTypesEnum[] | null>(null);
provide("sharedDataTypes", sharedDataTypes);

const route = useRoute();
watch(
  () => route.path,
  () => {
    shareStatus.value = "ok";
  },
);

// 共有リンク情報と共有元ユーザーの設定を取得する
// 設定は settingStore に格納し、通常画面と同じコンポーネントで参照できるようにする
const { getShareApi } = useApiClient();
const settingStore = useSettingStore();
const handleShareError = (err: unknown) => {
  if (err instanceof AxiosError && err.response?.status === 410) {
    shareStatus.value = "gone";
  }
};
watch(
  () => route.params.token as string | undefined,
  async (token) => {
    sharedDataTypes.value = null;
    settingStore.setting = null;
    if (!token) return;
    await Promise.all([
      getShareApi()
        .getShareInfo(token)
        .then((res) => (sharedDataTypes.value = [...res.data.dataTypes]))
        .catch(handleShareError),
      getShareApi()
        .getShareSetting(token)
        .then((res) => (settingStore.setting = res.data))
        .catch(handleShareError),
    ]);
  },
  { immediate: true },
);
</script>
