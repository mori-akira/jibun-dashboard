<template>
  <label v-show="label" :class="labelClass" :for="forId"
    ><span>{{ label }}</span
    ><span v-show="required" class="required" aria-hidden="true">*</span></label
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

<style lang="css" scoped>
.required {
  margin-left: 0.2rem;
  color: #f33;
  font-size: 0.8rem;
}
</style>
