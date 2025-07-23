<template>
  <div :class="['wrapper', wrapperClass]">
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
          { 'no-border': noDrawBorder },
          { error: errorMessage },
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

<style lang="css" scoped>
select {
  width: stretch;
  outline: none;
  border-bottom: solid 2px transparent;
  box-sizing: content-box;
  margin-left: 1rem;
  transition: 0.2s;
  background-color: white;
  padding: 0.2rem 0.5rem;
}

select:not(:focus) {
  border-color: #aaa;
}

select:focus {
  border-color: #333;
}

select:hover {
  cursor: pointer;
}

select.no-border {
  border: none !important;
}

select.error {
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
