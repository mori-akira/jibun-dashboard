<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center ml-4', pickersWrapperClass]">
      <VueDatePicker
        :model-value="dateFromObj"
        :format="format"
        :month-picker="true"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        :auto-position="autoPosition"
        @update:model-value="onChangeFrom"
      />
      <span class="mx-2">~</span>
      <VueDatePicker
        :model-value="dateToObj"
        :format="format"
        :month-picker="true"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        :auto-position="autoPosition"
        @update:model-value="onChangeTo"
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
  dateFrom?: string;
  dateTo?: string;
  wrapperClass?: string;
  labelClass?: string;
  pickersWrapperClass?: string;
}>();

const emit = defineEmits<{
  (event: "change:from" | "change:to", value?: string): void;
}>();

const format = (date: Date) => moment(date).format("YYYY-MM-DD");

const dateFromObj = ref();
const dateToObj = ref();
watch(
  () => props.dateFrom,
  () => {
    if (props.dateFrom) {
      const [yearStr, monthStr] = props.dateFrom.split("-");
      const year = Number(yearStr);
      const month = Number(monthStr) - 1;
      dateFromObj.value = { year, month };
    } else {
      dateFromObj.value = null;
    }
  },
  { immediate: true }
);
watch(
  () => props.dateTo,
  () => {
    if (props.dateTo) {
      const [yearStr, monthStr] = props.dateTo.split("-");
      const year = Number(yearStr);
      const month = Number(monthStr) - 1;
      dateToObj.value = { year, month };
    } else {
      dateToObj.value = null;
    }
  },
  { immediate: true }
);
const onChangeFrom = (data: { year: number; month: number } | null) => {
  if (data) {
    emit("change:from", format(new Date(data.year, data.month, 1)));
  } else {
    emit("change:from", undefined);
  }
};
const onChangeTo = (data: { year: number; month: number } | null) => {
  if (data) {
    emit("change:to", format(new Date(data.year, data.month, 1)));
  } else {
    emit("change:to", undefined);
  }
};
</script>
