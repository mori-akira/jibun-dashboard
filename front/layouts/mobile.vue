<template>
  <div class="flex flex-col min-h-screen bg-slate-50">
    <Header />
    <HeaderMenu />
    <Navigation />
    <main class="flex-1 overflow-y-auto px-3 pt-12 pb-14">
      <NuxtPage />
    </main>

    <LoadingOverlay
      :is-loading="commonStore.loadingQueue.length > 0"
      fullscreen
    />
    <ErrorMessageDialog
      wrapper-class="mt-12"
      :error-messages="commonStore.errorMessages"
      @close="commonStore.clearErrorMessages"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue-router";

import Header from "~/components/mobile/MobileHeader.vue";
import HeaderMenu from "~/components/mobile/MobileHeaderMenu.vue";
import Navigation from "~/components/mobile/MobileNavigation.vue";
import LoadingOverlay from "~/components/common/LoadingOverlay.vue";
import ErrorMessageDialog from "~/components/common/ErrorMessageDialog.vue";
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { useSettingStore } from "~/stores/setting";
import { withErrorHandling } from "~/utils/api-call";

const router = useRouter();
const commonStore = useCommonStore();
const userStore = useUserStore();
const settingStore = useSettingStore();

router.afterEach(() => {
  commonStore.setNavOpen(false);
  commonStore.setHeaderMenuOpen(false);
  commonStore.clearErrorMessages();
});

onMounted(async () => {
  await withErrorHandling(
    async () =>
      await Promise.all([userStore.fetchUser(), settingStore.fetchSetting()]),
    commonStore
  );
  commonStore.setNavOpen(false);
  commonStore.setHeaderMenuOpen(false);
});

watchEffect(() => {
  const baseTitle = "Jibun Dashboard";
  if (document?.title) {
    if (commonStore.hasUnsavedChange) {
      document.title = "*" + baseTitle;
    } else {
      document.title = baseTitle;
    }
  }
});
</script>
