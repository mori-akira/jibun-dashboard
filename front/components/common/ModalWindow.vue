<template>
  <div
    v-if="showModal"
    :class="[
      'fixed inset-0 bg-black/80 flex justify-center items-center z-[1001]',
      modalOverlayClass,
    ]"
    role="dialog"
    aria-modal="true"
    @click="onClose"
  >
    <div :class="['p-4 bg-white', modalBoxClass]" @click.stop>
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  showModal: boolean;
  modalOverlayClass?: string;
  modalBoxClass?: string;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const onClose = (): void => {
  emit("close");
};
</script>
