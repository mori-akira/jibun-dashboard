<template>
  <div
    v-if="showModal"
    class="modal-overlay"
    role="dialog"
    aria-modal="true"
    @click="onClose"
  >
    <div :class="['modal-box', modalBoxClass]" @click.stop>
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  showModal: boolean;
  modalBoxClass?: string;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const onClose = (): void => {
  emit("close");
};
</script>

<style lang="css" scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1001;
}

.modal-box {
  padding: 1rem;
  background-color: #fff;
}
</style>
