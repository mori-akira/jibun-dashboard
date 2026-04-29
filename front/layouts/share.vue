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
</script>
