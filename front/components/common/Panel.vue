<script setup lang="ts">
defineProps<{
  title?: string;
}>();
</script>

<template>
  <div class="panel">
    <h2 v-show="title">{{ title }}</h2>
    <slot />
  </div>
</template>

<style scoped>
.panel {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  flex-direction: column;
  background-color: #fff;
  margin: 1rem;
  padding: 1rem;
  border-radius: 0.5rem;
  box-shadow: 2px 2px 4px #000;
}
</style>
