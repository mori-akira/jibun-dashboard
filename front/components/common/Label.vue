<template>
  <label v-show="label" :class="labelClass" :for="forId"
    ><span>{{ label }}</span
    ><span
      v-show="required"
      class="ml-[0.2rem] text-[#f33] text-[0.8rem]"
      aria-hidden="true"
      >*</span
    ></label
  >
</template>

<script setup lang="ts">
defineProps<{
  label?: string;
  forId?: string;
  required?: boolean;
  labelClass?: string;
}>();
</script>
