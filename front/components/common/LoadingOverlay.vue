<template>
  <div
    v-if="isLoading"
    :class="[
      positionClass,
      wrapperClass,
      'bg-black/80 flex justify-center items-center',
    ]"
    role="status"
    aria-busy="true"
  >
    <div class="flex flex-col items-center">
      <Icon
        name="tabler:loader"
        class="w-16 h-16 text-white animate-spin [animation-duration:3s]"
      />
      <p
        v-if="message"
        class="mt-4 text-white text-sm text-center px-4 whitespace-pre-line font-cursive"
      >
        {{ message }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  isLoading: boolean;
  fullscreen?: boolean;
  message?: string;
  wrapperClass?: string;
}>();

const positionClass = computed(() =>
  props.fullscreen !== false
    ? "fixed inset-0 z-[1002]"
    : "absolute inset-0 z-[100]"
);
</script>
