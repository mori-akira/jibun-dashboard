<template>
  <div
    :class="[
      'p-4 rounded-lg bg-white shadow-[2px_2px_4px_#000]',
      { 'm-4': !noMargin },
      { 'mx-auto': centered },
      wrapperClass,
    ]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  centered?: boolean;
  noMargin?: boolean;
  wrapperClass?: string;
}>();
</script>
