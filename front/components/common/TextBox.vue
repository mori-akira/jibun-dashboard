<template>
  <div :class="['wrapper', wrapperClass]">
    <Label
      :label="label"
      :for-id="id"
      :required="required"
      :label-class="labelClass"
    />
    <div :class="[inputWrapperClass]">
      <input
        :id="id"
        ref="inputRef"
        :type="type || 'text'"
        :class="[
          Array.isArray(inputClass) ? inputClass.join(' ') : inputClass,
          { 'no-border': noDrawBorder },
          { error: errorMessage },
          { 'ml-4': label },
        ]"
        :value="value != null ? String(value) : ''"
        :aria-invalid="!!errorMessage"
        @change="onChangeValue($event)"
        @blur="onBlurValue($event)"
        @input="onInputValue($event)"
        @keydown.enter="onKeydownEnter"
        @keydown.escape="onKeydownEscape"
      />
      <span v-if="errorMessage" class="error-message">{{ errorMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { generateRandomString } from "~/utils/rand";
import Label from "~/components/common/Label.vue";

defineProps<{
  label?: string;
  required?: boolean;
  value?: string | number | null;
  type?: string;
  noDrawBorder?: boolean;
  errorMessage?: string;
  wrapperClass?: string;
  labelClass?: string;
  inputWrapperClass?: string;
  inputClass?: string | string[];
}>();

const emit = defineEmits<{
  (event: "change:value" | "blur:value" | "input:value", value: string): void;
  (event: "change:event" | "blur:event" | "input:event", e: Event): void;
  (event: "keydown:enter" | "keydown:escape"): void;
}>();

const inputRef = ref<HTMLInputElement>();
defineExpose({
  focus: async () => {
    await nextTick();
    inputRef.value?.focus();
  },
});
export type TextBoxExpose = {
  focus: () => void;
};

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
const onInputValue = (e: Event): void => {
  const target = e.target as HTMLInputElement;
  emit("input:value", target.value);
  emit("input:event", e);
};
const onKeydownEnter = (): void => {
  emit("keydown:enter");
};
const onKeydownEscape = (): void => {
  emit("keydown:escape");
};

const id = computed(() => `input-${generateRandomString()}`);
</script>

<style lang="css" scoped>
input {
  width: stretch;
  outline: none;
  border-bottom: solid 2px transparent;
  box-sizing: content-box;
  transition: 0.2s;
}

input:not(:focus) {
  border-color: #aaa;
}

input:focus {
  border-color: #333;
}

input.no-border {
  border: none !important;
}

input.error {
  border-color: #f33;
}

.wrapper {
  display: flex;
}

.error-message {
  display: inline-block;
  margin-left: 1rem;
  color: #f33;
  font-size: 0.8rem;
}
</style>
