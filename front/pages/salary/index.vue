<template>
  <div>
    <div class="flex justify-between">
      <Breadcrumb
        :items="[{ text: 'Salary', iconName: 'tabler:report-money' }]"
      />
      <div class="flex items-center mr-4">
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-32"
          @click="() => navigateTo('/salary/payslip')"
        >
          <Icon
            name="tabler:align-box-left-top"
            class="text-base translate-y-0.5"
          />
          <span class="font-cursive font-bold ml-2">Payslip</span>
        </Button>
        <Button
          type="navigation"
          size="small"
          html-type="button"
          button-class="w-32"
          wrapper-class="ml-2"
          @click="() => navigateTo('/salary/edit')"
        >
          <Icon name="tabler:database-edit" class="text-base translate-y-0.5" />
          <span class="font-cursive font-bold ml-2">Edit</span>
        </Button>
      </div>
    </div>

    <div class="flex justify-between">
      <Panel panel-class="w-full">
        <SelectBox
          label="Base Financial Year"
          :options="
            financialYears
              .map((year) => ({
                label: year,
                value: year,
              }))
              .toReversed()
          "
          :value="baseFinancialYear"
          wrapper-class="items-center"
          label-class="font-cursive"
          @change:value="baseFinancialYear = $event"
        />
      </Panel>
    </div>

    <div class="flex justify-between">
      <Panel panel-class="w-5/12">
        <h3>
          <Icon name="tabler:coin-yen" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Annual Income</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.grossIncome"
            :value-format="(value: number) => `￥${value.toLocaleString()}`"
            :base-financial-year="baseFinancialYear"
            wrapper-class="w-full"
          />
        </div>
      </Panel>
      <Panel panel-class="w-7/12">
        <h3>
          <Icon name="tabler:graph" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
        <div class="h-36 flex items-center">
          <TransitionGraph
            :labels="
              trimArray(targetFinancialYears, transitionItemCount, {
                from: 'end',
              })
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
          <Icon name="tabler:stopwatch" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Annual Overtime</span>
        </h3>
        <div class="h-36 flex items-center">
          <AnnualComparer
            :selector="(salary: Salary) => salary.overview.overtime"
            :value-format="(value: number) => `${value.toLocaleString()} H`"
            :base-financial-year="baseFinancialYear"
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
          <Icon name="tabler:graph" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Transition</span>
        </h3>
        <div class="h-36 flex items-center">
          <TransitionGraph
            :labels="
              trimArray(targetFinancialYears, transitionItemCount, {
                from: 'end',
              })
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
          <Icon name="tabler:clipboard-data" class="adjust-icon-4" />
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

    <div class="flex justify-between">
      <Panel panel-class="w-full overflow-x-auto">
        <h3>
          <Icon name="tabler:align-box-left-top" class="adjust-icon-4" />
          <span class="font-cursive font-bold ml-2">Payslip</span>
        </h3>
        <div class="h-128 flex justify-between items-center px-4 py-2">
          <template
            v-for="salary in trimArray(
              filterSalaryByFinancialYears(
                salaryStore.salaries ?? [],
                targetFinancialYears,
                financialYearStartMonth
              ).toReversed(),
              3
            )"
            :key="salary.salaryId"
          >
            <Payslip
              :salary="salary"
              wrapper-class="w-full h-full"
              title-class="font-cursive font-bold"
              headline-class="font-cursive"
              label-class="font-cursive"
            />
          </template>
        </div>
        <div class="h-10 flex justify-end items-end px-4">
          <AnchorLink
            link="/salary/payslip"
            text="View All Payslips"
            target="_self"
            anchor-class="font-cursive"
          />
        </div>
      </Panel>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Overview, Salary } from "~/api/client";
import { useCommonStore } from "~/stores/common";
import { useSettingStore } from "~/stores/setting";
import { useSalaryStore } from "~/stores/salary";
import Breadcrumb from "~/components/common/Breadcrumb.vue";
import Panel from "~/components/common/Panel.vue";
import Tabs from "~/components/common/Tabs.vue";
import SelectBox from "~/components/common/SelectBox.vue";
import AnchorLink from "~/components/common/AnchorLink.vue";
import Button from "~/components/common/Button.vue";
import TransitionGraph from "~/components/common/graph/Transition.vue";
import AnnualComparer from "~/components/salary/AnnualComparer.vue";
import OverviewCompareGraph from "~/components/salary/OverviewCompareGraph.vue";
import OverviewCompareSummary from "~/components/salary/OverviewCompareSummary.vue";
import Payslip from "~/components/salary/Payslip.vue";
import {
  getFinancialYears,
  filterFinancialYears,
  filterSalaryByFinancialYears,
  aggregateAnnually,
  aggregateCompareData,
} from "~/utils/salary";
import { trimArray } from "~/utils/array";
import { withErrorHandling } from "~/utils/api-call";

const commonStore = useCommonStore();
const settingStore = useSettingStore();
const salaryStore = useSalaryStore();

onMounted(async () => {
  withErrorHandling(() => salaryStore.fetchSalary(), commonStore);
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
const baseFinancialYear = ref(financialYears.value.at(-1) ?? "");
watch(
  () => financialYears.value,
  () => (baseFinancialYear.value = financialYears.value.at(-1) ?? ""),
  { immediate: true }
);
const targetFinancialYears = computed(() =>
  filterFinancialYears(financialYears.value, baseFinancialYear.value)
);
const aggregateOverviewAnnually = (key: keyof Overview) =>
  trimArray(targetFinancialYears.value, 7, { from: "end" }).map((year) => {
    return aggregateAnnually(
      salaryStore.salaries ?? [],
      (salary) => salary.overview?.[key],
      year,
      financialYearStartMonth.value
    );
  });
const annualIncomes = computed(() => aggregateOverviewAnnually("grossIncome"));
const annualOvertime = computed(() => aggregateOverviewAnnually("overtime"));

type TabSlot = "grossIncome" | "netIncome" | "operatingTime" | "overtime";
const aggregateCompareDataWrapper = (key: TabSlot) =>
  aggregateCompareData(
    salaryStore.salaries ?? [],
    trimArray(targetFinancialYears.value, 3, { from: "end" }),
    key,
    compareDataColors.value,
    financialYearStartMonth.value
  );
const compareData = computed(() => {
  return {
    grossIncome: aggregateCompareDataWrapper("grossIncome"),
    netIncome: aggregateCompareDataWrapper("netIncome"),
    operatingTime: aggregateCompareDataWrapper("operatingTime"),
    overtime: aggregateCompareDataWrapper("overtime"),
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
