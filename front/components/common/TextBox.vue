<template>
  <div class="wrapper">
    <label v-show="label">{{ label }}</label>
    <input
      type="text"
      :value="value"
      @change="onChangeValue($event)"
      @blur="onBlurValue($event)"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label?: string;
  value?: string;
}>();

const emit = defineEmits<{
  (event: "change:value" | "blur:value", value: string): void;
}>();

const onChangeValue = (e: Event): void => {
  const target = e.target as HTMLInputElement;
  emit("change:value", target.value);
};

const onBlurValue = (e: Event): void => {
  const target = e.target as HTMLInputElement;
  emit("blur:value", target.value);
};
</script>

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
