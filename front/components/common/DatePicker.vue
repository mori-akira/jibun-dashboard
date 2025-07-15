<template>
  <div :class="['flex items-center', wrapperClass]">
    <Label :label="label" :required="required" :label-class="labelClass" />
    <div :class="['flex items-center ml-4', pickersWrapperClass]">
      <VueDatePicker
        v-model="dateObj"
        :format="format"
        :month-picker="monthPicker"
        :enable-time-picker="false"
        :teleport="true"
        position="center"
        :auto-position="autoPosition"
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
  monthPicker?: boolean;
  autoPosition?: "top" | "bottom";
  date?: string;
  wrapperClass?: string;
  labelClass?: string;
  pickersWrapperClass?: string;
}>();

const emit = defineEmits<{
  (event: "change", value?: string): void;
}>();

const dateObj = ref<Date | null>(props.date ? new Date(props.date) : null);

watch(dateObj, (newVal) => {
  if (newVal) {
    emit("change", moment(newVal).format("YYYY-MM-DD"));
  } else {
    emit("change", undefined);
  }
});

const format = (date: Date) => moment(date).format("YYYY-MM-DD");
</script>
