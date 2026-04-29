<template>
  <div class="flex min-h-screen">
    <ShareHeader />
    <div class="w-screen flex bg-white">
      <ShareNavigation />
      <main class="flex-1 h-[calc(100vh-4rem)] mt-12 p-4 pr-8 overflow-y-scroll">
        <NuxtPage />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import ShareHeader from "~/components/app/AppShareHeader.vue";
import ShareNavigation from "~/components/app/AppShareNavigation.vue";
</script>
