<template>
  <div :class="['panel', panelClass, { center: centered }]">
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  centered?: boolean;
  panelClass?: string;
}>();
</script>

<style scoped>
.panel {
  width: 100%;
  margin: 1rem;
  padding: 1rem;
  border-radius: 0.5rem;
  background-color: #fff;
  box-shadow: 2px 2px 4px #000;
}

.center {
  margin: 1rem auto;
}
</style>
