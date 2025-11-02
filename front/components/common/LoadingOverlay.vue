<template>
  <div
    v-if="isLoading"
    class="fixed inset-0 bg-black/80 flex justify-center items-center z-[1002]"
    role="status"
    aria-busy="true"
  >
    <Icon
      name="tabler:loader"
      class="w-16 h-16 text-white animate-spin [animation-duration:3s]"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  isLoading: boolean;
}>();
</script>
