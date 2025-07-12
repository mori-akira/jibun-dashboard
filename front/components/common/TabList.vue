<template>
  <div :class="['flex border-b', wrapperClass]">
    <button
      v-for="(tab, index) in tabs"
      :key="index"
      :class="[
        'py-2 px-4',
        index === activeIndex
          ? 'border-b-2 border-blue-500 font-bold'
          : 'text-gray-500',
        buttonClass,
      ]"
      @click="$emit('change', index)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  tabs: { label: string }[];
  activeIndex: number;
  wrapperClass?: string;
  buttonClass?: string;
}>();

defineEmits<{
  (e: "change", index: number): void;
}>();
</script>

<style lang="css" scoped>
button:hover {
  cursor: pointer;
  background-color: #00000008;
}
</style>
