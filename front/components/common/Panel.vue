<template>
  <div :class="['panel', panelClass, { center: centered }]">
    <h2 v-show="title">{{ title }}</h2>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string;
  centered?: boolean;
  panelClass?: string;
}>();
</script>

<style scoped>
.panel {
  display: flex;
  background-color: #fff;
  margin: 1rem;
  padding: 1rem;
  border-radius: 0.5rem;
  box-shadow: 2px 2px 4px #000;
}

.center {
  margin: 1rem auto;
}
</style>
