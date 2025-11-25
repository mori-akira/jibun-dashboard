<template>
  <div
    :class="[
      'border-4 border-solid rounded-2xl flex items-center',
      wrapperClass,
      { 'border-blue-800 bg-blue-100': type === 'info' },
      { 'border-green-800 bg-green-100': type === 'confirm' },
      { 'border-orange-800 bg-yellow-100': type === 'warning' },
      { 'border-red-800 bg-red-100': type === 'error' },
    ]"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  type: "info" | "confirm" | "warning" | "error";
  wrapperClass?: string;
}>();
</script>
