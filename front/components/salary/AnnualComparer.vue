<template>
  <div :class="wrapperClass">
    <div class="w-full mt-4 ml-2">
      <span class="inline-block font-cursive w-1/2">This Year</span>
      <span
        v-if="salaries?.length"
        :class="[
          'inline-block',
          'font-bold',
          'w-1/2',
          {
            [positiveColorTextClass || 'text-blue-600']:
              annualAggregation.thisYear >= annualAggregation.lastYear,
          },
          {
            [negativeColorTextClass || 'text-red-600']:
              annualAggregation.thisYear < annualAggregation.lastYear,
          },
        ]"
        >{{ valueFormat(annualAggregation.thisYear) }}</span
      >
    </div>
    <CompareBar
      v-if="annualAggregation.thisYear >= annualAggregation.lastYear"
      :left-value="annualAggregation.lastYear"
      :right-value="annualAggregation.thisYear - annualAggregation.lastYear"
      wrapper-class="mt-4"
      left-class="bg-gray-600"
      :right-class="positiveColorBackgroundClass || 'bg-blue-600'"
    />
    <CompareBar
      v-else-if="annualAggregation.thisYear < annualAggregation.lastYear"
      :left-value="annualAggregation.thisYear"
      :right-value="annualAggregation.lastYear - annualAggregation.thisYear"
      wrapper-class="mt-4 ml-2 mr-2"
      :left-class="negativeColorBackgroundClass || 'bg-red-600'"
      right-class="bg-gray-600"
    />
    <div class="w-full mt-4 ml-2">
      <span class="inline-block font-cursive w-1/2">Last Year</span>
      <span v-if="salaries?.length" class="inline-block font-bold w-1/2">{{
        valueFormat(annualAggregation.lastYear)
      }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Salary } from "~/api/client";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import CompareBar from "~/components/common/CompareBar.vue";
import { getFinancialYears, aggregateAnnually } from "~/utils/salary";

const props = defineProps<{
  selector: (salary: Salary) => number;
  valueFormat: (value: number) => string;
  baseFinancialYear?: string;
  positiveColorTextClass?: string;
  negativeColorTextClass?: string;
  positiveColorBackgroundClass?: string;
  negativeColorBackgroundClass?: string;
  wrapperClass?: string;
}>();

const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

const financialYearStartMonth = computed(
  () => settingStore.setting?.salary.financialYearStartMonth ?? 1
);
const salaries = computed(() => salaryStore.salaries ?? []);
const years = computed(() =>
  getFinancialYears(salaries.value ?? [], financialYearStartMonth.value)
);
const thisYear = computed(
  () => props.baseFinancialYear ?? years.value.at(-1) ?? ""
);
const lastYear = computed(() =>
  props.baseFinancialYear
    ? (Number(props.baseFinancialYear) - 1).toString()
    : years.value.at(-2) ?? ""
);
const annualAggregation = computed(() => ({
  thisYear: aggregateAnnually(
    salaries.value,
    props.selector,
    thisYear.value,
    financialYearStartMonth.value
  ),
  lastYear: aggregateAnnually(
    salaries.value,
    props.selector,
    lastYear.value,
    financialYearStartMonth.value
  ),
}));
</script>
