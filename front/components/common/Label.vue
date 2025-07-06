<template>
  <label v-show="label" :class="labelClass" :for="forId"
    >{{ label }}<span v-show="required" class="required">*</span></label
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

<style scoped>
.required {
  margin-left: 0.2rem;
  color: #f33;
  font-size: 0.8rem;
}
</style>
