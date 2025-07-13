<template>
  <TextBox
    :label="label"
    :required="required"
    :value="value"
    :no-draw-border="true"
    :error-message="errorMessage"
    :wrapper-class="wrapperClass"
    :label-class="labelClass"
    :input-wrapper-class="inputWrapperClass"
    :input-class="`border-none cursor-pointer ${inputClass ?? ''}`"
    type="color"
    @input:event="onInputValue"
  />
</template>

<script setup lang="ts">
import TextBox from "~/components/common/TextBox.vue";

const props = defineProps<{
  label?: string;
  required?: boolean;
  value?: string;
  format?: "hex" | "rgba";
  errorMessage?: string;
  wrapperClass?: string;
  labelClass?: string;
  inputWrapperClass?: string;
  inputClass?: string;
}>();

const emit = defineEmits<{
  (event: "input:value", value: string): void;
  (event: "input:event", e: Event): void;
}>();

const hexToRgba = (hex: string): string => {
  let r = 0,
    g = 0,
    b = 0;
  if (hex.length === 4) {
    r = parseInt(hex[1] + hex[1], 16);
    g = parseInt(hex[2] + hex[2], 16);
    b = parseInt(hex[3] + hex[3], 16);
  } else if (hex.length === 7) {
    r = parseInt(hex.slice(1, 3), 16);
    g = parseInt(hex.slice(3, 5), 16);
    b = parseInt(hex.slice(5, 7), 16);
  }
  return `rgba(${r}, ${g}, ${b}, 1)`;
};

const convertValue = (hex: string) => {
  if (props.format === "rgba") {
    return hexToRgba(hex);
  } else {
    return hex.toUpperCase();
  }
};

const onInputValue = (e: Event) => {
  const target = e.target as HTMLInputElement;
  target.value = convertValue(target.value);
  emit("input:event", e);
  emit("input:value", target.value);
};
</script>
