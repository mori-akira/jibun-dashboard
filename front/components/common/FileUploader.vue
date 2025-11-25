<template>
  <div
    :class="[
      'wrapper w-full rounded-2xl flex justify-center items-center cursor-pointer bg-gray-100 border-4 border-dashed border-gray-400',
      wrapperClass,
    ]"
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
