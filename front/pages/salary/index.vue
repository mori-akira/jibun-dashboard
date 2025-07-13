<template>
  <div>
    <h2>
      <Icon name="tabler:report-money" class="adjust-icon" />
      <span class="font-cursive font-bold ml-2">Salary</span>
    </h2>
    <div class="flex justify-between">
      <Panel panel-class="w-full"> </Panel>
    </div>

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
            :labels="
              trimArray(financialYears, transitionItemCount, { from: 'end' })
            "
            :values="
              trimArray(annualIncomes, transitionItemCount, { from: 'end' })
            "
            :y-axis-min="0"
            :show-y-grid="true"
            wrapper-class="w-full h-36 mt-4"
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
            :labels="
              trimArray(financialYears, transitionItemCount, { from: 'end' })
            "
            :values="
              trimArray(annualOvertime, transitionItemCount, { from: 'end' })
            "
            :y-axis-min="0"
            :show-y-grid="true"
            wrapper-class="w-full h-36 mt-4"
          />
        </div>
      </Panel>
    </div>

    <div class="flex justify-between">
      <Panel panel-class="w-9/12">
        <h3>
          <Icon name="tabler:clipboard-data" class="adjust-icon" />
          <span class="font-cursive font-bold ml-2">Compare</span>
        </h3>
        <Tabs
          :tabs="[
            { label: 'Gross Income', slot: 'grossIncome' },
            { label: 'Net Income', slot: 'netIncome' },
            { label: 'Operating Time', slot: 'operatingTime' },
            { label: 'Overtime', slot: 'overtime' },
          ]"
          :init-tab="selectedTab"
          button-class="font-cursive"
          @change:tab="onChangeTab"
        >
          <template #grossIncome>
            <div class="mt-4">
              <OverviewCompareGraph
                :datasets="compareData.grossIncome"
                :y-axis-max="incomeMaxRange"
              />
            </div>
          </template>
          <template #netIncome>
            <div class="mt-4">
              <OverviewCompareGraph
                :datasets="compareData.netIncome"
                :y-axis-max="incomeMaxRange"
              />
            </div>
          </template>
          <template #operatingTime>
            <div class="mt-4">
              <OverviewCompareGraph :datasets="compareData.operatingTime" />
            </div>
          </template>
          <template #overtime>
            <div class="mt-4">
              <OverviewCompareGraph :datasets="compareData.overtime" />
            </div>
          </template>
        </Tabs>
      </Panel>
      <Panel panel-class="w-3/12">
        <OverviewCompareSummary :summary-data="summaryData" />
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
import Tabs from "~/components/common/Tabs.vue";
import TransitionGraph from "~/components/common/graph/Transition.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import OverviewCompareGraph from "~/components/salary/OverviewCompareGraph.vue";
import OverviewCompareSummary from "~/components/salary/OverviewCompareSummary.vue";
import {
  getFinancialYears,
  aggregateAnnually,
  aggregateCompareData,
} from "~/utils/salary";
import { trimArray } from "~/utils/trim-array";

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

onMounted(async () => {
  commonStore.setLoading(true);
  await salaryStore.fetchSalary();
  commonStore.setLoading(false);
});

const financialYearStartMonth = computed(
  () => settingStore.setting?.salary.financialYearStartMonth ?? 1
);
const transitionItemCount = computed(
  () => settingStore.setting?.salary.transitionItemCount ?? 1
);
const compareDataColors = computed(() => {
  const colors = settingStore.setting?.salary.compareDataColors ?? [];
  return [colors?.[0] ?? "#ddd", colors?.[1] ?? "#ddd", colors?.[2] ?? "#ddd"];
});
const financialYears = computed(() =>
  getFinancialYears(salaryStore.salaries ?? [], financialYearStartMonth.value)
);
const annualIncomes = computed(() =>
  trimArray(financialYears.value, 7, { from: "end" }).map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview.grossIncome,
      year,
      financialYearStartMonth.value
    );
  })
);
const annualOvertime = computed(() =>
  trimArray(financialYears.value, 7, { from: "end" }).map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview.overtime,
      year,
      financialYearStartMonth.value
    );
  })
);

type TabSlot = "grossIncome" | "netIncome" | "operatingTime" | "overtime";
const compareData = computed(() => {
  return {
    grossIncome: aggregateCompareData(
      salaryStore.salaries ?? [],
      trimArray(financialYears.value, 3, { from: "end" }),
      "grossIncome",
      compareDataColors.value,
      financialYearStartMonth.value
    ),
    netIncome: aggregateCompareData(
      salaryStore.salaries ?? [],
      trimArray(financialYears.value, 3, { from: "end" }),
      "netIncome",
      compareDataColors.value,
      financialYearStartMonth.value
    ),
    operatingTime: aggregateCompareData(
      salaryStore.salaries ?? [],
      trimArray(financialYears.value, 3, { from: "end" }),
      "operatingTime",
      compareDataColors.value,
      financialYearStartMonth.value
    ),
    overtime: aggregateCompareData(
      salaryStore.salaries ?? [],
      trimArray(financialYears.value, 3, { from: "end" }),
      "overtime",
      compareDataColors.value,
      financialYearStartMonth.value
    ),
  };
});
const incomeMaxRange = computed(() => {
  const maxValue = compareData.value.grossIncome.reduce(
    (max: number, dataset) => Math.max(max, ...dataset.data),
    0
  );
  return Math.ceil(maxValue / 100000) * 100000;
});

const selectedTab = ref<TabSlot>("grossIncome");
const onChangeTab = (slot: string) => {
  selectedTab.value = slot as TabSlot;
};
const summaryData = computed<{ label: string; value: number }[]>(() => {
  const targetData = compareData.value[selectedTab.value];
  return targetData.toReversed().map((dataset) => ({
    label: dataset.label,
    value: dataset.data.reduce((sum, value) => sum + value, 0),
  }));
});
</script>
