<template>
  <div :class="['wrapper', wrapperClass]">
    <h4 :class="titleClass">{{ title }}</h4>

    <h5 :class="headlineClass">Overview</h5>
    <div v-if="salary?.overview.grossIncome" class="row">
      <div :class="['label', labelClass]">Gross Income</div>
      <div :class="['value', valueClass]">
        {{ salary?.overview?.grossIncome?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.overview.netIncome" class="row">
      <div :class="['label', labelClass]">Net Income</div>
      <div :class="['value', valueClass]">
        {{ salary?.overview?.netIncome?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.overview.operatingTime" class="row">
      <div :class="['label', labelClass]">Operating Time</div>
      <div :class="['value', valueClass]">
        {{ salary?.overview?.operatingTime?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.overview.overtime" class="row">
      <div :class="['label', labelClass]">Overtime</div>
      <div :class="['value', valueClass]">
        {{ salary?.overview?.overtime?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.overview.bonus" class="row">
      <div :class="['label', labelClass]">Bonus</div>
      <div :class="['value', valueClass]">
        {{ salary?.overview?.bonus?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.overview.bonusTakeHome" class="row">
      <div :class="['label', labelClass]">Bonus (Take Home)</div>
      <div :class="['value', valueClass]">
        {{ salary?.overview?.bonusTakeHome?.toLocaleString() }}
      </div>
    </div>

    <h5 :class="headlineClass">Structure</h5>
    <div v-if="salary?.structure.basicSalary" class="row">
      <div :class="['label', labelClass]">Basic Salary</div>
      <div :class="['value', valueClass]">
        {{ salary?.structure?.basicSalary?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.structure.overtimePay" class="row">
      <div :class="['label', labelClass]">Overtime Pay</div>
      <div :class="['value', valueClass]">
        {{ salary?.structure?.overtimePay?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.structure.housingAllowance" class="row">
      <div :class="['label', labelClass]">Housing Allowance</div>
      <div :class="['value', valueClass]">
        {{ salary?.structure?.housingAllowance?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.structure.positionAllowance" class="row">
      <div :class="['label', labelClass]">Position Allowance</div>
      <div :class="['value', valueClass]">
        {{ salary?.structure?.positionAllowance?.toLocaleString() }}
      </div>
    </div>
    <div v-if="salary?.structure.other" class="row">
      <div :class="['label', labelClass]">Other</div>
      <div :class="['value', valueClass]">
        {{ salary?.structure?.other?.toLocaleString() }}
      </div>
    </div>

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
import type { Salary } from "~/api/client";
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
