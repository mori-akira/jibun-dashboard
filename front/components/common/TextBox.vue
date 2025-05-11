<script setup lang="ts">
defineProps<{
  label?: string;
}>();

const emit = defineEmits<{
  (event: "onChange:value"): void;
  (event: "onBlur:value"): void;
}>();

const onChangeValue = (e): void => emit("onChange:value", e.target.value);
const onBlurValue = (e): void => emit("onBlur:value", e.target.value);
</script>

<template>
  <div class="wrapper">
    <label v-show="label">{{ label }}</label>
    <input
      type="text"
      @change="onChangeValue($event)"
      @blur="onBlurValue($event)"
    />
  </div>
</template>

<style scoped>
.wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin: 1rem;
}

.wrapper > label {
  width: 20%;
}

.wrapper > input {
  margin-left: 1rem;
}
</style>
