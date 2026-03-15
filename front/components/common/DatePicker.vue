<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center', pickersWrapperClass]">
      <VueDatePicker
        :model-value="dateObj"
        :format="format"
        v-bind="config"
        @update:model-value="onChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import VueDatePicker from "@vuepic/vue-datepicker";
import "@vuepic/vue-datepicker/dist/main.css";

import Label from "~/components/common/Label.vue";
import { useDatePickerConfig } from "~/composables/common/useDatePickerConfig";

const props = defineProps<{
  label?: string;
  required?: boolean;
  autoPosition?: "top" | "bottom";
  textInput?: boolean;
  date?: string;
  wrapperClass?: string;
  labelClass?: string;
  pickersWrapperClass?: string;
}>();

const emit = defineEmits<{
  (event: "change:date", value?: string): void;
}>();

const { format, config } = useDatePickerConfig(props);

const dateObj = ref();
watch(
  () => props.date,
  () => {
    dateObj.value = props.date ? new Date(props.date) : null;
  },
  { immediate: true },
);
const onChange = (data: Date | null) => {
  if (data) {
    emit("change:date", format(data));
  } else {
    emit("change:date", undefined);
  }
};
</script>
