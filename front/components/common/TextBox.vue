<template>
  <div :class="['flex', wrapperClass]">
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
          'w-full outline-none border-b-2 box-content transition duration-200',
          errorMessage ? 'border-[#f33]' : 'border-[#aaa] focus:border-[#333]',
          { 'border-0': noDrawBorder },
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
