<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center', pickersWrapperClass]">
      <VueDatePicker
        :model-value="dateFromObj"
        :format="format"
        v-bind="config"
        @update:model-value="onChangeFrom"
      />
      <span class="mx-2">~</span>
      <VueDatePicker
        :model-value="dateToObj"
        :format="format"
        v-bind="config"
        @update:model-value="onChangeTo"
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
  dateFrom?: string;
  dateTo?: string;
  wrapperClass?: string;
  labelClass?: string;
  pickersWrapperClass?: string;
}>();

const emit = defineEmits<{
  (event: "change:from" | "change:to", value?: string): void;
}>();

const { format, config } = useDatePickerConfig(props);

const dateFromObj = ref();
const dateToObj = ref();
watch(
  () => props.dateFrom,
  () => {
    dateFromObj.value = props.dateFrom ? new Date(props.dateFrom) : null;
  },
  { immediate: true },
);
watch(
  () => props.dateTo,
  () => {
    dateToObj.value = props.dateTo ? new Date(props.dateTo) : null;
  },
  { immediate: true },
);
const onChangeFrom = (data: Date | null) => {
  if (data) {
    emit("change:from", format(data));
  } else {
    emit("change:from", undefined);
  }
};
const onChangeTo = (data: Date | null) => {
  if (data) {
    emit("change:to", format(data));
  } else {
    emit("change:to", undefined);
  }
};
</script>
