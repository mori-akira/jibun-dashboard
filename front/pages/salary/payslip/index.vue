<template>
  <div>
    <Breadcrumb
      :items="[
        { text: 'Salary', iconName: 'tabler:report-money', link: '/salary' },
        { text: 'Payslip', iconName: 'tabler:align-box-left-top' },
      ]"
    />

    <div class="flex justify-between">
      <Panel panel-class="w-full">
        <DatePickerFromTo
          label="Scope"
          month-picker
          :date-from="dateFrom"
          :date-to="dateTo"
          label-class="w-20 font-cursive"
          pickers-wrapper-class="min-w-96 w-1/2"
          @change:from="onChangeDateFrom"
          @change:to="onChangeDateTo"
        />
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import DatePickerFromTo from "~/components/common/DatePickerFromTo.vue";

const dateFrom = ref<string | undefined>("");
const onChangeDateFrom = async (value: string | undefined) => {
  dateFrom.value = value;
};

const dateTo = ref<string | undefined>("");
const onChangeDateTo = async (value: string | undefined) => {
  dateTo.value = value;
};
</script>
