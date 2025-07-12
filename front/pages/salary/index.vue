<template>
  <div>
    <h2>
      <Icon name="tabler:report-money" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Salary</span>
    </h2>

    <div class="flex justify-between">
      <Panel panel-class="w-5/12">
        <h3>
          <Icon name="tabler:coin-yen" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Annual Income</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.grossIncome"
            :value-format="(value: number) => `￥${value.toLocaleString()}`"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
      <Panel panel-class="w-7/12">
        <h3>
          <Icon name="tabler:graph" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
        <div class="h-36 flex items-center">
          <TransitionGraph
            :labels="years"
            :values="annualIncomes"
            :show-y-grid="true"
            wrapper-class="w-full h-36"
          />
        </div>
      </Panel>
    </div>

    <div class="flex justify-between">
      <Panel panel-class="w-5/12">
        <h3>
          <Icon name="tabler:stopwatch" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Annual Overtime</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.overtime"
            :value-format="(value: number) => `${value.toLocaleString()} H`"
            positive-color-text-class="text-red-600"
            negative-color-text-class="text-blue-600"
            positive-color-background-class="bg-red-600"
            negative-color-background-class="bg-blue-600"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
      <Panel panel-class="w-7/12">
        <h3>
          <Icon name="tabler:graph" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
        <div class="h-36 flex items-center">
          <TransitionGraph
            :labels="years"
            :values="annualOvertime"
            :show-y-grid="true"
            wrapper-class="w-full h-36"
          />
        </div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import type { Salary } from "~/api/client";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import Panel from "~/components/common/Panel.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import TransitionGraph from "~/components/common/graph/Transition.vue";
import { getFinancialYears, aggregateAnnually } from "~/utils/salary";

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

const financialYearStartMonth = computed(
  () => settingStore.setting?.salary.financialYearStartMonth ?? 1
);
const years = computed(() =>
  getFinancialYears(salaryStore.salaries ?? [], financialYearStartMonth.value)
);
const annualIncomes = computed(() => {
  return years.value.map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview.grossIncome,
      year,
      financialYearStartMonth.value
    );
  });
});
const annualOvertime = computed(() => {
  return years.value.map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview.overtime,
      year,
      financialYearStartMonth.value
    );
  });
});

onMounted(async () => {
  commonStore.setLoading(true);
  await salaryStore.fetchSalary();
  commonStore.setLoading(false);
});
</script>

<style lang="css" scoped>
.row {
  width: 100%;
  margin-top: 1rem;
}
</style>
