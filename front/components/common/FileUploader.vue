<template>
  <div
    :class="['wrapper', wrapperClass]"
    @dragover.prevent
    @drop.prevent="handleDrop"
    @click="triggerFileInput"
  >
    <slot />
    <input
      ref="fileInput"
      type="file"
      class="hidden"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  wrapperClass?: string;
}>();

const emit = defineEmits<{
  (e: "upload", file: File): void;
}>();

const fileInput = ref<HTMLInputElement>();

const triggerFileInput = () => {
  fileInput.value?.click();
};
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files?.[0]) {
    await uploadFile(target.files[0]);
    target.value = "";
  }
};
const handleDrop = async (event: DragEvent) => {
  const file = event.dataTransfer?.files?.[0];
  if (file) {
    await uploadFile(file);
  }
};
const uploadFile = async (file: File) => {
  emit("upload", file);
};
</script>

<style lang="css" scoped>
.wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  border: 4px dashed #aaa;
  border-radius: 1rem;
  background-color: #eee;
  cursor: pointer;
}
</style>
