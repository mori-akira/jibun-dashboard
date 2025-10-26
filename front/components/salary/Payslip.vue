<template>
  <div :class="['wrapper', wrapperClass]">
    <h4 :class="titleClass">{{ title }}</h4>

    <h5 :class="headlineClass">Overview</h5>
    <template v-for="def in overviewDef">
      <div
        v-if="salary?.overview?.[def.value]"
        :key="`overview-${def.label}`"
        class="row"
      >
        <div :class="['label', labelClass]">{{ def.label }}</div>
        <div :class="['value', valueClass]">
          {{ salary?.overview?.[def.value]?.toLocaleString() }}
        </div>
      </div>
    </template>

    <h5 :class="headlineClass">Structure</h5>
    <template v-for="def in structureDef">
      <div
        v-if="salary?.structure?.[def.value]"
        :key="`structure-${def.label}`"
        class="row"
      >
        <div :class="['label', labelClass]">{{ def.label }}</div>
        <div :class="['value', valueClass]">
          {{ salary?.structure?.[def.value]?.toLocaleString() }}
        </div>
      </div>
    </template>

    <template v-for="group in salary?.payslipData" :key="group.key">
      <h5 :class="headlineClass">{{ group.key }}</h5>
      <div v-for="item in group.data" :key="item.key" class="row">
        <div :class="['label', labelClass]">{{ item.key }}</div>
        <div :class="['value', valueClass]">
          {{ item?.data?.toLocaleString() }}
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import type { Salary, Overview, Structure } from "~/generated/api/client";
import { getYearMonthAsNumber } from "~/utils/salary";

const props = defineProps<{
  salary?: Salary;
  titleClass?: string;
  headlineClass?: string;
  wrapperClass?: string;
  labelClass?: string;
  valueClass?: string;
}>();

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

<style lang="css" scoped>
h5 {
  margin-top: 1rem;
  border-bottom: 1px solid #333;
}

.wrapper {
  min-width: 21rem;
  margin: 1rem 0.5rem 0.5rem;
  padding: 1rem;
  background-color: #fff;
  box-shadow: 1px 1px 2px #000;
  overflow-y: auto;
}

.row {
  width: 100%;
  display: flex;
  margin-top: 0.3rem;
  padding-left: 0.5rem;
}

.row:nth-of-type(even) {
  background-color: #eee;
}

.label {
  width: 50%;
}

.value {
  width: 50%;
}
</style>
