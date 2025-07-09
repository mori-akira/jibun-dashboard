<template>
  <div
    :class="['dialog-box', { show: errorMessages?.length }]"
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
      <Icon name="tabler:x" class="close-icon text-4xl" @click="onClose"></Icon>
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

<style scoped>
.dialog-box {
  display: flex;
  justify-content: space-between;
  position: fixed;
  top: 1rem;
  left: 10%;
  width: 80%;
  padding: 1rem;
  background-color: #f33;
  transition: 0.5s;
  z-index: 1000;
}

.dialog-box:not(.show) {
  opacity: 0;
  transform: translateY(-6rem);
}

.close-icon {
  color: white;
}

.close-icon:hover {
  cursor: pointer;
  color: #ccc;
}
</style>
