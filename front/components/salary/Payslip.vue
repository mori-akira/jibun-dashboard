<template>
  <div
    :class="[
      'm-4 p-4 bg-white shadow-[1px_1px_2px_#000] overflow-y-auto',
      wrapperClass,
    ]"
  >
    <h4 :class="titleClass">{{ title }}</h4>

    <h5 :class="['mt-4 border-b border-[#333]', headlineClass]">Overview</h5>
    <template v-for="def in overviewDef">
      <div
        v-if="salary?.overview?.[def.value]"
        :key="`overview-${def.label}`"
        class="w-full flex mt-[0.3rem] pl-2 [&:nth-of-type(even)]:bg-[#eee]"
      >
        <div :class="['w-1/2', labelClass]">{{ def.label }}</div>
        <div :class="['w-1/2', valueClass]">
          {{ salary?.overview?.[def.value]?.toLocaleString() }}
        </div>
      </div>
    </template>

    <h5 :class="['mt-4 border-b border-[#333]', headlineClass]">Structure</h5>
    <template v-for="def in structureDef">
      <div
        v-if="salary?.structure?.[def.value]"
        :key="`structure-${def.label}`"
        class="w-full flex mt-[0.3rem] pl-2 [&:nth-of-type(even)]:bg-[#eee]"
      >
        <div :class="['w-1/2', labelClass]">{{ def.label }}</div>
        <div :class="['w-1/2', valueClass]">
          {{ salary?.structure?.[def.value]?.toLocaleString() }}
        </div>
      </div>
    </template>

    <template v-for="group in salary?.payslipData" :key="group.key">
      <h5 :class="['mt-4 border-b border-[#333]', headlineClass]">
        {{ group.key }}
      </h5>
      <div
        v-for="item in group.data"
        :key="item.key"
        class="w-full flex mt-[0.3rem] pl-2 [&:nth-of-type(even)]:bg-[#eee]"
      >
        <div :class="['w-1/2', labelClass]">{{ item.key }}</div>
        <div :class="['w-1/2', valueClass]">
          {{ item?.data?.toLocaleString() }}
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { Salary, Overview, Structure } from "~/generated/api/client";
import { getYearMonthAsNumber } from "~/utils/salary";

const props = withDefaults(
  defineProps<{
    salary?: Salary;
    titleClass?: string;
    headlineClass?: string;
    wrapperClass?: string;
    labelClass?: string;
    valueClass?: string;
  }>(),
  {
    salary: undefined,
    titleClass: "",
    headlineClass: "",
    wrapperClass: "min-w-[21rem]",
    labelClass: "",
    valueClass: "",
  },
);

const monthMapper = (month: number) => {
  const months = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
  ];
  return months[month - 1];
};
const title = computed(() => {
  const [_, month] = getYearMonthAsNumber(props.salary?.targetDate ?? "");
  return monthMapper(month ?? 0);
});
const overviewDef: { label: string; value: keyof Overview }[] = [
  {
    label: "Gross Income",
    value: "grossIncome",
  },
  {
    label: "Net Income",
    value: "netIncome",
  },
  {
    label: "Operating Time",
    value: "operatingTime",
  },
  {
    label: "Overtime",
    value: "overtime",
  },
  {
    label: "Bonus",
    value: "bonus",
  },
  {
    label: "Bonus (Take Home)",
    value: "bonusTakeHome",
  },
];
const structureDef: { label: string; value: keyof Structure }[] = [
  {
    label: "Basic Salary",
    value: "basicSalary",
  },
  {
    label: "Overtime Pay",
    value: "overtimePay",
  },
  {
    label: "Housing Allowance",
    value: "housingAllowance",
  },
  {
    label: "Position Allowance",
    value: "positionAllowance",
  },
  {
    label: "Other",
    value: "other",
  },
];
</script>
