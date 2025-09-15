<template>
  <div v-if="isLoading" class="loading-overlay" role="status" aria-busy="true">
    <Icon name="tabler:loader" class="loading-icon" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  isLoading: boolean;
}>();
</script>

<style lang="css" scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1002;
}

.loading-icon {
  width: 4rem;
  height: 4rem;
  color: #fff;
  animation: rotation 3s linear infinite;
}

@keyframes rotation {
  0% {
    transform: rotate(0);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
