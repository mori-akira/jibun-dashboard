<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center ml-4', pickersWrapperClass]">
      <VueDatePicker
        :model-value="dateObj"
        :format="format"
        month-picker
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
    if (props.date) {
      const [yearStr, monthStr] = props.date.split("-");
      const year = Number(yearStr);
      const month = Number(monthStr) - 1;
      dateObj.value = { year, month };
    } else {
      dateObj.value = null;
    }
  },
  { immediate: true },
);
const onChange = (data: { year: number; month: number } | null) => {
  if (data) {
    emit("change:date", format(new Date(data.year, data.month, 1)));
  } else {
    emit("change:date", undefined);
  }
};
</script>
