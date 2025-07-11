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
          { 'text-blue-600': yearIncome.thisYear >= yearIncome.lastYear },
          { 'text-red-600': yearIncome.thisYear < yearIncome.lastYear },
        ]"
        >{{ `￥${yearIncome.thisYear.toLocaleString()}` }}</span
      >
    </div>
    <CompareBar
      v-if="yearIncome.thisYear >= yearIncome.lastYear"
      :left-value="yearIncome.lastYear"
      :right-value="yearIncome.thisYear - yearIncome.lastYear"
      wrapper-class="mt-4"
      left-class="bg-gray-600"
      right-class="bg-blue-600"
    />
    <CompareBar
      v-else-if="yearIncome.thisYear < yearIncome.lastYear"
      :left-value="yearIncome.thisYear"
      :right-value="yearIncome.lastYear - yearIncome.thisYear"
      wrapper-class="mt-4 ml-2 mr-2"
      left-class="bg-red-600"
      right-class="bg-gray-600"
    />
    <div class="w-full mt-4 ml-2">
      <span class="inline-block font-cursive w-1/2">Last Year</span>
      <span v-if="salaries?.length" class="inline-block font-bold w-1/2">{{
        `￥${yearIncome.lastYear.toLocaleString()}`
      }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Salary } from "~/api/client";
import CompareBar from "~/components/common/CompareBar.vue";
import { getYears, getTotalYearIncome } from "~/utils/salary";

const props = defineProps<{
  salaries: Salary[];
  wrapperClass?: string;
}>();

const years = computed(() => getYears(props.salaries ?? []));
const thisYear = computed(() => years.value.at(-1) ?? "");
const lastYear = computed(() => years.value.at(-2) ?? "");
const yearIncome = computed(() => ({
  thisYear: getTotalYearIncome(props.salaries ?? [], thisYear.value),
  lastYear: getTotalYearIncome(props.salaries ?? [], lastYear.value),
}));
</script>
