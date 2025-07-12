<template>
  <div :class="['wrapper', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['pickers-wrapper', pickersWrapperClass]">
      <VueDatePicker
        v-model="dateFromObj"
        :format="format"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        auto-position="top"
      />
      <span>~</span>
      <VueDatePicker
        v-model="dateToObj"
        :format="format"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        auto-position="top"
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
  dateFrom?: string;
  dateTo?: string;
  wrapperClass?: string;
  labelClass?: string;
  pickersWrapperClass?: string;
}>();

const emit = defineEmits<{
  (event: "change:from" | "change:to", value?: string): void;
}>();

const dateFromObj = ref<Date | null>(
  props.dateFrom ? new Date(props.dateFrom) : null
);
const dateToObj = ref<Date | null>(
  props.dateTo ? new Date(props.dateTo) : null
);

watch(dateFromObj, (newVal) => {
  if (newVal) {
    emit("change:from", moment(newVal).format("YYYY-MM-DD"));
  } else {
    emit("change:from", undefined);
  }
});

watch(dateToObj, (newVal) => {
  if (newVal) {
    emit("change:to", moment(newVal).format("YYYY-MM-DD"));
  } else {
    emit("change:to", undefined);
  }
});

const format = (date: Date) => moment(date).format("YYYY-MM-DD");
</script>

<style lang="css" scoped>
.wrapper {
  display: flex;
  align-items: center;
}

.pickers-wrapper {
  display: flex;
  align-items: center;
}

.pickers-wrapper > * {
  margin-left: 1rem;
}
</style>
