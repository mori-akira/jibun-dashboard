<template>
  <a :href="link" :class="[anchorClass]" :target="target">
    <span>{{ text }}</span>
    <Icon name="tabler:external-link" class="link-icon" />
  </a>
</template>

<script setup lang="ts">
defineProps<{
  link: string;
  text: string;
  target?: "_blank" | "_self" | "_parent" | "_top";
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
