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
        :type="type || 'text'"
        :class="[inputClass, { error: errorMessage }]"
        :value="value"
        :aria-invalid="!!errorMessage"
        @change="onChangeValue($event)"
        @blur="onBlurValue($event)"
      />
      <span v-if="errorMessage" class="error-message">{{ errorMessage }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { generateRandomString } from "~/utils/rand";
import Label from "~/components/common/Label.vue";

const props = defineProps<{
  label?: string;
  required?: boolean;
  value?: string;
  type?: string;
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
  if (props.value !== target.value) {
    emit("change:value", target.value);
    emit("change:event", e);
  }
};

const onBlurValue = (e: Event): void => {
  const target = e.target as HTMLInputElement;
  if (props.value !== target.value) {
    emit("blur:value", target.value);
    emit("blur:event", e);
  }
};

const id = computed(() => `input-${generateRandomString()}`);
</script>

<style lang="css" scoped>
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
}

.error-message {
  display: inline-block;
  margin-left: 1rem;
  color: #f33;
  font-size: 0.8rem;
}
</style>
