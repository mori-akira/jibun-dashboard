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
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useCommonStore } from "~/stores/common";
import { useUserStore } from "~/stores/user";
import Header from "~/components/common/AppHeader.vue";
import Navigation from "~/components/common/AppNavigation.vue";
import HeaderMenu from "~/components/common/HeaderMenu.vue";

const router = useRouter();
const commonStore = useCommonStore();
const userStore = useUserStore();

router.afterEach(() => {
  commonStore.setHeaderMenuOpen(false);
});

onMounted(() => {
  userStore.fetchUser();
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
