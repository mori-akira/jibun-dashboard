<template>
  <div class="w-full h-full flex flex-col justify-around items-center p-4">
    <div
      v-for="(data, index) in summaryData"
      :key="index"
      class="w-full flex justify-center"
      :style="{ color: data.color }"
    >
      <span class="font-cursive">{{ data.label }}</span>
      <span class="font-bold ml-4">
        {{ data.value.toLocaleString() }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  summaryData: { label: string; value: number; color: string }[];
}>();
</script>
