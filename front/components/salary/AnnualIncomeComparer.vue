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
          { 'text-blue-600': annualIncome.thisYear >= annualIncome.lastYear },
          { 'text-red-600': annualIncome.thisYear < annualIncome.lastYear },
        ]"
        >{{ `￥${annualIncome.thisYear.toLocaleString()}` }}</span
      >
    </div>
    <CompareBar
      v-if="annualIncome.thisYear >= annualIncome.lastYear"
      :left-value="annualIncome.lastYear"
      :right-value="annualIncome.thisYear - annualIncome.lastYear"
      wrapper-class="mt-4"
      left-class="bg-gray-600"
      right-class="bg-blue-600"
    />
    <CompareBar
      v-else-if="annualIncome.thisYear < annualIncome.lastYear"
      :left-value="annualIncome.thisYear"
      :right-value="annualIncome.lastYear - annualIncome.thisYear"
      wrapper-class="mt-4 ml-2 mr-2"
      left-class="bg-red-600"
      right-class="bg-gray-600"
    />
    <div class="w-full mt-4 ml-2">
      <span class="inline-block font-cursive w-1/2">Last Year</span>
      <span v-if="salaries?.length" class="inline-block font-bold w-1/2">{{
        `￥${annualIncome.lastYear.toLocaleString()}`
      }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import CompareBar from "~/components/common/CompareBar.vue";
import { getFinancialYears, getTotalAnnualIncome } from "~/utils/salary";

defineProps<{
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
const thisYear = computed(() => years.value.at(-1) ?? "");
const lastYear = computed(() => years.value.at(-2) ?? "");
const annualIncome = computed(() => ({
  thisYear: getTotalAnnualIncome(
    salaries.value,
    thisYear.value,
    financialYearStartMonth.value
  ),
  lastYear: getTotalAnnualIncome(
    salaries.value,
    lastYear.value,
    financialYearStartMonth.value
  ),
}));
</script>
