<template>
  <div :class="['flex', wrapperClass]">
    <Label
      :label="label"
      :for-id="id"
      :required="required"
      :label-class="labelClass"
    />
    <div :class="[selectWrapperClass]">
      <select
        :id="id"
        :class="[
          selectClass,
          'w-full outline-none border-b-2 box-content transition duration-200 bg-white py-[0.2rem] px-2',
          errorMessage ? 'border-[#f33]' : 'border-[#aaa] focus:border-[#333]',
          { 'border-0': noDrawBorder },
          { 'ml-4': label },
          'hover:cursor-pointer',
        ]"
        :value="value"
        :aria-invalid="!!errorMessage"
        @change="onChangeValue($event)"
      >
        <option v-if="placeholder" disabled value="">{{ placeholder }}</option>
        <option
          v-for="(option, index) in options"
          :key="index"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
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
  value?: string;
  placeholder?: string;
  options: { label: string; value: string }[];
  noDrawBorder?: boolean;
  errorMessage?: string;
  wrapperClass?: string;
  labelClass?: string;
  selectWrapperClass?: string;
  selectClass?: string;
}>();

const emit = defineEmits<{
  (event: "change:value", value: string): void;
  (event: "change:event", e: Event): void;
}>();

const onChangeValue = (e: Event): void => {
  const target = e.target as HTMLSelectElement;
  if (props.value !== target.value) {
    emit("change:value", target.value);
    emit("change:event", e);
  }
};

const id = computed(() => `select-${generateRandomString()}`);
</script>
