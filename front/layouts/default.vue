<template>
  <div class="layout">
    <Header />
    <HeaderMenu />
    <div class="content" @click="commonStore.setHeaderMenuOpen(false)">
      <Navigation />
      <main>
        <NuxtPage />
      </main>
    </div>

    <LoadingOverlay :is-loading="commonStore.loadingQueue.length > 0" />
    <ErrorMessageDialog
      :error-messages="commonStore.errorMessages"
      @close="commonStore.clearErrorMessages"
    />
    <Dialog
      :show-dialog="showConfirmDialog"
      type="confirm"
      :message="confirmDialogMessage"
      button-type="yesNo"
      @click:yes="onConfirmYes"
      @click:no="onConfirmNo"
      @close="onConfirmNo"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";

import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import { useSettingStore } from "~/stores/setting";
import Header from "~/components/app/AppHeader.vue";
import Navigation from "~/components/app/AppNavigation.vue";
import HeaderMenu from "~/components/app/HeaderMenu.vue";
import LoadingOverlay from "~/components/common/LoadingOverlay.vue";
import ErrorMessageDialog from "~/components/common/ErrorMessageDialog.vue";
import Dialog from "~/components/common/Dialog.vue";
import { useConfirmDialog } from "~/composables/useConfirmDialog";

const router = useRouter();
const commonStore = useCommonStore();
const userStore = useUserStore();
const settingStore = useSettingStore();

const {
  showConfirmDialog,
  confirmDialogMessage,
  openConfirmDialog,
  onConfirmYes,
  onConfirmNo,
} = useConfirmDialog();
router.beforeEach(async (to, _, next) => {
  if (commonStore.hasUnsavedChange) {
    next(false);
    const confirmed = await openConfirmDialog(
      "You Have Unsaved Change. Continue?"
    );
    if (confirmed) {
      commonStore.setHasUnsavedChange(false);
      router.push(to.fullPath);
    }
  } else {
    next();
  }
});
router.afterEach(() => {
  commonStore.setHeaderMenuOpen(false);
  commonStore.clearErrorMessages();
});

onMounted(async () => {
  await Promise.all([userStore.fetchUser(), settingStore.fetchSetting()]);
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

<style lang="css" scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.content {
  width: 100vw;
  display: flex;
  background-color: #fff;
}

main {
  flex: 1;
  height: calc(100vh - 4rem);
  margin-top: 3rem;
  padding: 1rem;
  padding-right: 2rem;
  overflow-y: scroll;
}
</style>
