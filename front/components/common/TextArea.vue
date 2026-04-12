<template>
  <div :class="['flex', wrapperClass]">
    <Label
      :label="label"
      :for-id="id"
      :required="required"
      :label-class="labelClass"
    />
    <div :class="[inputWrapperClass]">
      <textarea
        :id="id"
        :rows="rows ?? 4"
        :class="[
          Array.isArray(inputClass) ? inputClass.join(' ') : inputClass,
          'w-full outline-none border-b-2 box-content transition duration-200 resize-y',
          errorMessage ? 'border-[#f33]' : 'border-[#aaa] focus:border-[#333] focus:bg-indigo-50',
          { 'ml-4': label },
        ]"
        :value="actualValue != null ? String(actualValue) : ''"
        :aria-invalid="!!errorMessage"
        @change="onChangeValue($event)"
        @blur="onBlurValue($event)"
        @input="onInputValue($event)"
      />
      <span
        v-if="errorMessage"
        class="inline-block ml-4 text-[#f33] text-[0.8rem]"
        >{{ errorMessage }}</span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
import { generateRandomString } from "~/utils/rand";
import Label from "~/components/common/Label.vue";

const props = defineProps<{
  label?: string;
  required?: boolean;
  modelValue?: string | null;
  value?: string | null;
  rows?: number;
  errorMessage?: string;
  wrapperClass?: string;
  labelClass?: string;
  inputWrapperClass?: string;
  inputClass?: string | string[];
}>();

const emit = defineEmits<{
  (
    event: "update:modelValue" | "change:value" | "blur:value" | "input:value",
    value: string,
  ): void;
  (event: "change:event" | "blur:event" | "input:event", e: Event): void;
}>();

const actualValue = computed(() => props.modelValue ?? props.value);

const onChangeValue = (e: Event): void => {
  const target = e.target as HTMLTextAreaElement;
  emit("update:modelValue", target.value);
  emit("change:value", target.value);
  emit("change:event", e);
};
const onBlurValue = (e: Event): void => {
  const target = e.target as HTMLTextAreaElement;
  emit("blur:value", target.value);
  emit("blur:event", e);
};
const onInputValue = (e: Event): void => {
  const target = e.target as HTMLTextAreaElement;
  emit("input:value", target.value);
  emit("input:event", e);
};

const id = `textarea-${generateRandomString()}`;
</script>
