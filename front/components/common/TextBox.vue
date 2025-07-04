<template>
  <div :class="['wrapper', wrapperClass]">
    <label v-show="label" :class="labelClass" :for="id"
      >{{ label }}<span v-show="required" class="required">*</span></label
    >
    <div :class="[inputWrapperClass]">
      <input
        :id="id"
        type="text"
        :class="[inputClass, { error: errorMessage }]"
        :value="value"
        @change="onChangeValue($event)"
        @blur="onBlurValue($event)"
      />
      <span class="error-message">{{ errorMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { generateRandomString } from "~/util/rand";

defineProps<{
  label?: string;
  value?: string;
  required?: boolean;
  errorMessage?: string;
  wrapperClass?: string;
  labelClass?: string;
  inputWrapperClass?: string;
  inputClass?: string;
}>();

const emit = defineEmits<{
  (event: "change:value" | "blur:value", value: string): void;
  (event: "change:event" | "blur:event", e: Event): void;
}>();

const onChangeValue = (e: Event): void => {
  const target = e.target as HTMLInputElement;
  emit("change:value", target.value);
  emit("change:event", e);
};

const onBlurValue = (e: Event): void => {
  const target = e.target as HTMLInputElement;
  emit("blur:value", target.value);
  emit("blur:event", e);
};

const id = generateRandomString();
</script>

<style scoped>
input {
  width: stretch;
  outline: none;
  border-bottom: solid 2px transparent;
  box-sizing: content-box;
  margin-left: 1rem;
  transition: 0.2s;
}

input:not(:focus) {
  border-color: #aaa;
}

input:focus {
  border-color: #333;
}

input.error {
  border-color: #f33;
}

.wrapper {
  display: flex;
  justify-content: flex-start;
  align-items: start;
  margin: 1rem;
}

.required {
  margin-left: 0.2rem;
  color: #f33;
  font-size: 0.8rem;
}

.error-message {
  margin-left: 1rem;
  color: #f33;
  font-size: 0.8rem;
}
</style>
