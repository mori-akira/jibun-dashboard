<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center', pickersWrapperClass]">
      <VueDatePicker
        :model-value="dateObj"
        :format="format"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        :auto-position="autoPosition"
        :text-input="
          textInput && {
            format: 'yyyy-MM-dd',
            enterSubmit: true,
            selectOnFocus: true,
            openMenu: 'toggle',
            escClose: true,
          }
        "
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
  textInput?: boolean;
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
    dateObj.value = props.date ? new Date(props.date) : null;
  },
  { immediate: true, deep: true }
);
const onChange = (data: Date | null) => {
  if (data) {
    emit("change", format(data));
  } else {
    emit("change", undefined);
  }
};
</script>
