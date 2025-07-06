<template>
  <a :href="link" :class="[anchorClass]" target="_blank">
    <span>{{ text }}</span>
    <Icon name="tabler:external-link" class="link-icon" />
  </a>
</template>

<script setup lang="ts">
defineProps<{
  link: string;
  text: string;
  anchorClass?: string;
}>();
</script>

<style scoped>
a {
  color: #33f;
  text-decoration: underline;
}

.link-icon {
  transform: translate(2px, 3px);
}
</style>
