<template>
  <div
    :class="[
      'fixed top-4 left-[10%] w-[80%] p-4 bg-[#f33] z-[1000] transition duration-500 flex justify-between',
      errorMessages?.length
        ? 'opacity-100 translate-y-0'
        : 'opacity-0 -translate-y-24 pointer-events-none',
    ]"
    aria-live="assertive"
  >
    <div class="flex items-center justify-start">
      <div class="flex justify-center items-center w-8">
        <Icon name="tabler:alert-circle" class="text-4xl text-white" />
      </div>
      <div class="ml-4">
        <p
          v-for="(errorMessage, index) in errorMessages"
          :key="index"
          class="font-bold text-white"
        >
          {{ errorMessage }}
        </p>
      </div>
    </div>
    <div class="flex justify-center items-center w-6">
      <Icon
        name="tabler:x"
        class="text-white hover:text-[#ccc] cursor-pointer text-4xl"
        @click="onClose"
      ></Icon>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  errorMessages?: string[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const onClose = (): void => {
  emit("close");
};
</script>
