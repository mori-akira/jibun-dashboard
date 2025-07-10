<template>
  <div :class="['flex', 'justify-center', wrapperClass]">
    <div
      :class="['h-1.5', leftClass]"
      :style="{ width: `${leftValuePercent}%` }"
    ></div>
    <div
      :class="['h-1.5', rightClass]"
      :style="{ width: `${rightValuePercent}%` }"
    ></div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  leftValue: number;
  rightValue: number;
  wrapperClass?: string;
  leftClass?: string;
  rightClass?: string;
}>();

const leftValuePercent =
  (props.leftValue * 100) / (props.leftValue + props.rightValue);
const rightValuePercent =
  (props.rightValue * 100) / (props.leftValue + props.rightValue);
</script>
