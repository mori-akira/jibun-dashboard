<template>
  <div class="layout">
    <Header />
    <HeaderMenu />
    <Navigation />
    <div class="content" @click="commonStore.setHeaderMenuOpen(false)">
      <main>
        <NuxtPage />
      </main>
    </div>
    <LoadingOverlay :is-loading="commonStore.isLoading" />
    <ErrorMessageDialog
      :error-messages="commonStore.errorMessages"
      @close="commonStore.clearErrorMessages"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";

import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import Header from "~/components/app/AppHeader.vue";
import Navigation from "~/components/app/AppNavigation.vue";
import HeaderMenu from "~/components/app/HeaderMenu.vue";
import LoadingOverlay from "~/components/common/LoadingOverlay.vue";
import ErrorMessageDialog from "~/components/common/ErrorMessageDialog.vue";

const router = useRouter();
const commonStore = useCommonStore();
const userStore = useUserStore();

router.afterEach(() => {
  commonStore.setHeaderMenuOpen(false);
  commonStore.clearErrorMessages();
});

onMounted(async () => {
  await userStore.fetchUser();
});
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.content {
  flex: 1;
  padding: 20px;
  background-color: #fff;
}

main {
  margin-top: 3rem;
  padding: 1rem;
}
</style>
