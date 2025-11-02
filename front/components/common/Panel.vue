<template>
  <div
    :class="[
      'm-4 p-4 rounded-lg bg-white shadow-[2px_2px_4px_#000]',
      { 'mx-auto': centered },
      panelClass,
    ]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  centered?: boolean;
  panelClass?: string;
}>();
</script>
