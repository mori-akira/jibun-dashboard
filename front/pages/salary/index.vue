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
            :labels="trimArray(years, transitionItemCount, { from: 'end' })"
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
            :labels="trimArray(years, transitionItemCount, { from: 'end' })"
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
              <div class="h-36 flex items-center">
                <CompareBarGraph
                  :labels="compareDataHeaders"
                  :datasets="compareData.grossIncome"
                  :y-axis-min="0"
                  :y-axis-max="incomeMaxRange"
                  :show-y-grid="true"
                  wrapper-class="w-full h-36 mt-4"
                />
              </div>
            </div>
          </template>
          <template #netIncome>
            <div class="mt-4">
              <div class="h-36 flex items-center">
                <CompareBarGraph
                  :labels="compareDataHeaders"
                  :datasets="compareData.netIncome"
                  :y-axis-min="0"
                  :y-axis-max="incomeMaxRange"
                  :show-y-grid="true"
                  wrapper-class="w-full h-36 mt-4"
                />
              </div>
            </div>
          </template>
          <template #operatingTime>
            <div class="mt-4">
              <div class="h-36 flex items-center">
                <CompareBarGraph
                  :labels="compareDataHeaders"
                  :datasets="compareData.operatingTime"
                  :show-y-grid="true"
                  wrapper-class="w-full h-36 mt-4"
                />
              </div>
            </div>
          </template>
          <template #overtime>
            <div class="mt-4">
              <div class="h-36 flex items-center">
                <CompareBarGraph
                  :labels="compareDataHeaders"
                  :datasets="compareData.overtime"
                  :show-y-grid="true"
                  wrapper-class="w-full h-36 mt-4"
                />
              </div>
            </div>
          </template>
        </Tabs>
      </Panel>
      <Panel panel-class="w-3/12">
        <div
          class="w-full h-full flex flex-col justify-around items-center p-4"
        >
          <div
            v-for="(data, index) in summaryData"
            :key="index"
            class="w-full flex justify-center"
            :style="{ color: compareDataColors.toReversed()[index] }"
          >
            <span class="font-cursive">{{ data.label }}</span>
            <span class="font-bold ml-4">
              {{ data.value.toLocaleString() }}
            </span>
          </div>
        </div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";

import type { Salary, Overview } from "~/api/client";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import Panel from "~/components/common/Panel.vue";
import Tabs from "~/components/common/Tabs.vue";
import TransitionGraph from "~/components/common/graph/Transition.vue";
import CompareBarGraph from "~/components/common/graph/CompareBar.vue";
import type { DataSet as CompareBarGraphDataSet } from "~/components/common/graph/CompareBar.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import {
  getFinancialYears,
  getMonthsInFinancialYear,
  aggregateAnnually,
  filterSalaryByFinancialYearMonth,
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
const years = computed(() =>
  getFinancialYears(salaryStore.salaries ?? [], financialYearStartMonth.value)
);
const annualIncomes = computed(() =>
  trimArray(years.value, 7, { from: "end" }).map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview.grossIncome,
      year,
      financialYearStartMonth.value
    );
  })
);
const annualOvertime = computed(() =>
  trimArray(years.value, 7, { from: "end" }).map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview.overtime,
      year,
      financialYearStartMonth.value
    );
  })
);

const aggregateCompareData = (
  targetYears: string[],
  label: keyof Overview,
  backgroundColor: string[]
): CompareBarGraphDataSet[] => {
  return targetYears.map((year, index) => {
    return {
      label: `FY${year}`,
      backgroundColor: backgroundColor[index],
      data: getMonthsInFinancialYear(
        year,
        financialYearStartMonth.value,
        true
      ).map((date) => {
        const salary = filterSalaryByFinancialYearMonth(
          salaryStore.salaries ?? [],
          date
        );
        return salary ? salary?.overview?.[label] : 0;
      }),
    };
  });
};
const compareDataHeaders = [
  "Apr",
  "May",
  "Jun",
  "Jul",
  "Aug",
  "Sep",
  "Oct",
  "Nov",
  "Dec",
  "Jan",
  "Feb",
  "Mar",
];
type TabSlot = "grossIncome" | "netIncome" | "operatingTime" | "overtime";
const compareData = computed(() => {
  return {
    grossIncome: aggregateCompareData(
      trimArray(years.value, 3, { from: "end" }),
      "grossIncome",
      compareDataColors.value
    ),
    netIncome: aggregateCompareData(
      trimArray(years.value, 3, { from: "end" }),
      "netIncome",
      compareDataColors.value
    ),
    operatingTime: aggregateCompareData(
      trimArray(years.value, 3, { from: "end" }),
      "operatingTime",
      compareDataColors.value
    ),
    overtime: aggregateCompareData(
      trimArray(years.value, 3, { from: "end" }),
      "overtime",
      compareDataColors.value
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
