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

const total = props.leftValue + props.rightValue;
const leftValuePercent =
  total === 0 ? undefined : (props.leftValue * 100) / total;
const rightValuePercent =
  total === 0 ? undefined : (props.rightValue * 100) / total;
</script>
