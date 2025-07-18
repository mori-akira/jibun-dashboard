<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center ml-4', pickersWrapperClass]">
      <VueDatePicker
        :model-value="dateObj"
        :format="format"
        :month-picker="true"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        :auto-position="autoPosition"
        @update:model-value="onChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import VueDatePicker from "@vuepic/vue-datepicker";
import "@vuepic/vue-datepicker/dist/main.css";
import moment from "moment";

import Label from "~/components/common/Label.vue";

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
  (event: "change", value?: string): void;
}>();

const format = (date: Date) => moment(date).format("YYYY-MM-DD");

const dateObj = ref();
watch(
  () => props.date,
  () => {
    if (props.date) {
      const [yearStr, monthStr] = props.date.split("-");
      const year = Number(yearStr);
      const month = Number(monthStr) - 1;
      dateObj.value = { year, month };
    }
  },
  { immediate: true }
);
const onChange = (data: { year: number; month: number } | null) => {
  if (data) {
    emit("change", format(new Date(data.year, data.month, 1)));
  } else {
    emit("change", undefined);
  }
};
</script>
